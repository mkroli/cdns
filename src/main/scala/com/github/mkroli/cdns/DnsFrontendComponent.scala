/*
 * Copyright 2014 Michael Krolikowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mkroli.cdns

import com.github.mkroli.dns4s.dsl._
import com.github.mkroli.dns4s.section.ResourceRecord

import akka.actor._
import akka.pattern._

trait DnsFrontendComponent {
  self: AkkaComponent with CassandraComponent =>

  lazy val dnsFrontendActor = system.actorOf(Props(new DnsFrontendActor))

  object QTypeString {
    def unapply(t: Int) = Map[Int, (String, (String) => ResourceRecordModifier)](
      ResourceRecord.typeA -> ("A", ARecord.apply),
      ResourceRecord.typeAAAA -> ("AAAA", AAAARecord.apply),
      ResourceRecord.typeNS -> ("NS", NSRecord.apply),
      ResourceRecord.typeCNAME -> ("CNAME", CNameRecord.apply),
      ResourceRecord.typePTR -> ("PTR", PTRRecord.apply)).get(t)
  }

  private class DnsFrontendActor extends Actor {
    import context.dispatcher

    override def receive = {
      case Query(Questions(q @ QName(qn) ~ QType(QTypeString(typeString, resourceFactory)) :: Nil)) =>
        (cassandraActor ? CassandraQuery(qn, typeString)).map {
          case CassandraResponse(answers, shuffle) =>
            val shuffledAnswers = if (shuffle) answers.toIndexedSeq.shuffle else answers
            Response ~ Answers(shuffledAnswers.map(resourceFactory(_): ResourceRecord): _*)
        }.recover {
          case _: NoSuchElementException => Response ~ NameError
          case _ => Response ~ ServerFailure
        }.map(_ ~ Questions(q: _*)).pipeTo(sender)
      case Query(Questions(q)) =>
        sender ! Response ~ NotImplemented ~ Questions(q: _*)
    }
  }
}
