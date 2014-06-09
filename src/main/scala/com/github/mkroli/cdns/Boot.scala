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

import com.github.mkroli.dns4s.akka.Dns

import akka.io.IO
import akka.pattern.ask

object Boot extends App {
  if (args.isEmpty) {
    System.err.println("Usage: cdns <cassandra1> [<cassandraN> ...]")
  } else {
    val applicationContext = new AnyRef with AkkaComponent with DnsFrontendComponent with CassandraComponent {
      override val cassandraHosts = args.toList

      IO(Dns) ? Dns.Bind(dnsFrontendActor, 53)
    }

    applicationContext.cluster
  }
}
