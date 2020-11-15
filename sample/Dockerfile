# Using the base image from https://hub.docker.com/_/adoptopenjdk
FROM adoptopenjdk/openjdk11:jre-11.0.8_10-alpine
LABEL maintainer="KM"
COPY paxos_echo_client-jar-with-dependencies.jar echo_client.jar
COPY conf conf
ENTRYPOINT [ "java", "-jar", "/echo_client.jar"]