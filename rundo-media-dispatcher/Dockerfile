FROM harbor.runjian.com/syy-open/ubuntu-jdk-11:20.04
ARG DEBIAN_FRONTEND=noninteractive
ENV TZ=Asia/Shanghai

EXPOSE 18081/tcp

ENV LC_ALL zh_CN.UTF-8
RUN apt update && apt install -y language-pack-zh-hans vim
RUN mkdir -pv /opt/rundo-meidia-dispatcher/logs/
COPY target/*.jar /opt/rundo-meidia-dispatcher
WORKDIR /opt/rundo-meidia-dispatcher



ENTRYPOINT ["/bin/bash","-c","java ${JAVA_OPTS} -jar /opt/rundo-meidia-dispatcher/*.jar  --spring.config.location=/opt/rundo-meidia-dispatcher/config/ ${0} ${@}"]
