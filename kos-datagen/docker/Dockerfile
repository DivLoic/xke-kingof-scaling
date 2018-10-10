FROM openjdk:8u181

MAINTAINER Loïc DIVAD ldivad@xebia.fr

RUN mkdir -p /etc/xke-kingof-scaling/ /opt/

COPY xke-kingof-scaling.tar /tmp/
COPY config.yaml /etc/xke-kingof-scaling/
COPY jmx_prometheus_javaagent-0.3.1.jar /etc/xke-kingof-scaling/

RUN tar -xf /tmp/xke-kingof-scaling.tar -C /opt/

WORKDIR /opt/xke-kingof-scaling/

ENTRYPOINT [ "/opt/xke-kingof-scaling/bin/xke-kingof-scaling" ]