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

import scala.collection.JavaConverters.asScalaBufferConverter

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.querybuilder.QueryBuilder

import akka.actor.Actor
import akka.actor.Props
import akka.pattern.pipe

trait CassandraComponent {
  self: AkkaComponent =>

  val cassandraHosts: List[String]

  lazy val cassandraActor = system.actorOf(Props(new CassandraActor))

  case class CassandraQuery(question: String, `type`: String)
  case class CassandraResponse(answers: List[String], shuffle: Boolean)

  lazy val cluster = {
    val cluster = (Cluster.builder /: cassandraHosts)(_ addContactPoint _).build.connect

    cluster.execute("""CREATE KEYSPACE IF NOT EXISTS cdns WITH replication = {
      'class': 'SimpleStrategy',
      'replication_factor': 1
    }""")

    cluster.execute("""CREATE COLUMNFAMILY IF NOT EXISTS cdns.cdns (
      question text,
      type text,
      answers list<text>,
      shuffle boolean,
      PRIMARY KEY (question, type)
    )""")

    cluster
  }

  private lazy val query = cluster.prepare(QueryBuilder
    .select("answers", "shuffle")
    .from("cdns", "cdns")
    .where(QueryBuilder.eq("question", QueryBuilder.bindMarker))
    .and(QueryBuilder.eq("type", QueryBuilder.bindMarker)))

  private class CassandraActor extends Actor {
    import context.dispatcher

    override def receive = {
      case CassandraQuery(q, t) =>
        cluster.executeAsync(query.bind(q, t)).map(r => Option(r.one)).collect {
          case Some(row) =>
            val answers = row.getList("answers", classOf[String]).asScala.toList
            val shuffle = row.getBool("shuffle")
            CassandraResponse(answers, shuffle)
        }.pipeTo(sender)
    }
  }
}
