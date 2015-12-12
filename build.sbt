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
name := "cdns"

organization := "com.github.mkroli"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation")

resolvers += "bintray" at "http://jcenter.bintray.com"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",
  "com.github.mkroli" %% "dns4s-akka" % "0.9",
  "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.9",
  "org.xerial.snappy" % "snappy-java" % "1.1.2",
  "net.jpountz.lz4" % "lz4" % "1.3"
)

packSettings

packMain := Map("cdns" -> "com.github.mkroli.cdns.Boot")

packJvmOpts := Map("cdns" -> Seq("-Dlogback.configurationFile=${PROG_HOME}/etc/logback.xml"))

packGenerateWindowsBatFile := false

packArchiveExcludes ++= Seq("Makefile", "VERSION")
