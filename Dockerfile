FROM dockerfile/java
MAINTAINER mkroli

ENV JAVA_HOME /usr/lib/jvm/java-7-oracle

ADD build.sbt /tmp/cdns/build.sbt
ADD project/plugins.sbt /tmp/cdns/project/plugins.sbt
ADD src /tmp/cdns/src

WORKDIR /tmp/cdns
RUN wget http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.2/sbt-launch.jar -O sbt.jar && \
    java -XX:MaxPermSize=128m -jar sbt.jar packArchive && \
    mkdir -p /opt/cdns && \
    tar --strip-components=1 -C /opt/cdns -xzf /tmp/cdns/target/cdns*.tar.gz && \
    rm -rf /root/.ivy /root/.sbt /tmp/cdns

WORKDIR /opt/cdns
EXPOSE 53
ENTRYPOINT /opt/cdns/bin/cdns ${CASSANDRA_PORT_9160_TCP_ADDR}
