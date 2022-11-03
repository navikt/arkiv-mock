FROM ghcr.io/navikt/baseimages/temurin:17

ENV SPRING_PROFILES_ACTIVE=docker
ENV KAFKA_BROKERS=kafka-broker:29092

COPY target/*.jar app.jar
EXPOSE 8092
