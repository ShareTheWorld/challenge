#基础镜像
FROM ubuntu:18.04
#FROM openjdk:9-jre

#将jdk8包放入/usr/local/src并自动解压，jdk8.tar.gz 需要到oracle官方下载，注意解压后的java版本号
ADD jdk11.tar.gz /

COPY challenge.jar /

WORKDIR /

COPY start.sh /

RUN chmod +x /start.sh

ENTRYPOINT ["/bin/bash", "/start.sh"]