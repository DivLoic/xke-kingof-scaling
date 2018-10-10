FROM openjdk:8u181

MAINTAINER Loïc DIVAD ldivad@xebia.fr

RUN mkdir -p /etc/kos-datagen/ /opt/

COPY kos-datagen*.tar /tmp/

RUN tar -xf /tmp/kos-datagen*.tar -C /opt/

RUN mv /opt/kos-datagen*/ /opt/kos-datagen/

WORKDIR /opt/kos-datagen/

ENTRYPOINT [ "/opt/kos-datagen/bin/kos-datagen" ]