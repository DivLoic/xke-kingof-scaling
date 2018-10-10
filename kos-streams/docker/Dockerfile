FROM openjdk:8u181

MAINTAINER Loïc DIVAD ldivad@xebia.fr

RUN mkdir -p /etc/kos-streams/ /opt/

COPY kos-streams*.tar /tmp/
COPY config.yaml /etc/kos-streams/
COPY jmx_prometheus_javaagent-0.3.1.jar /etc/kos-streams/

RUN tar -xf /tmp/kos-streams*.tar -C /opt/

RUN mv /opt/kos-streams*/ /opt/kos-streams/

WORKDIR /opt/kos-streams/

ENTRYPOINT [ "/opt/kos-streams/bin/kos-streams" ]