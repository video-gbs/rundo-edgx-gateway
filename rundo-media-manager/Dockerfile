FROM harbor.runjian.com/syy-open/ubuntu-jdk-11:20.04
ARG DEBIAN_FRONTEND=noninteractive
ENV TZ=Asia/Shanghai

EXPOSE 18090/tcp

ENV LC_ALL zh_CN.UTF-8
RUN apt update && apt install -y language-pack-zh-hans vim
RUN mkdir -pv /opt/rundo-media-manager/logs/
COPY target/*.jar /opt/rundo-media-manager
WORKDIR /opt/rundo-media-manager



ENTRYPOINT ["/bin/bash","-c","java ${JAVA_OPTS} -jar /opt/rundo-media-manager/*.jar  --spring.config.location=/opt/rundo-media-manager/config/ ${0} ${@}"]
