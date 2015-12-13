FROM java:openjdk-8-jre
MAINTAINER mkroli

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

ADD https://raw.githubusercontent.com/paulp/sbt-extras/master/sbt /usr/bin/sbt
RUN chmod 0755 /usr/bin/sbt

ADD build.sbt /tmp/cdns/build.sbt
ADD project/plugins.sbt /tmp/cdns/project/plugins.sbt
ADD src /tmp/cdns/src

WORKDIR /tmp/cdns
RUN sbt packArchiveTgz && \
    mkdir -p /opt/cdns && \
    tar --strip-components=1 -C /opt/cdns -xzf /tmp/cdns/target/cdns*.tar.gz && \
    rm -rf /root/.ivy /root/.sbt /tmp/cdns

WORKDIR /opt/cdns
EXPOSE 53
ENTRYPOINT /opt/cdns/bin/cdns ${CASSANDRA_PORT_9160_TCP_ADDR}
