#FROM harbor.runjian.com/syy-open/ubuntu-jre-11:20.04
FROM harbor.runjian.com/syy-open/ubuntu-jdk-11:20.04
ARG DEBIAN_FRONTEND=noninteractive
ENV TZ=Asia/Shanghai

EXPOSE 18080/tcp

EXPOSE 5060/tcp
EXPOSE 5060/udp
ENV LC_ALL zh_CN.UTF-8
RUN apt update && apt install -y language-pack-zh-hans vim
RUN mkdir -pv /opt/gb28181-server/logs/
COPY target/*.jar /opt/gb28181-server
WORKDIR /opt/gb28181-server



ENTRYPOINT ["/bin/bash","-c","java ${JAVA_OPTS} -jar /opt/gb28181-server/*.jar  --spring.config.location=/opt/gb28181-server/config/ ${0} ${@}"]
