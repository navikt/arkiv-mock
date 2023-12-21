FROM gcr.io/distroless/java21-debian12:nonroot

ENV SPRING_PROFILES_ACTIVE=docker
ENV KAFKA_BROKERS=kafka-broker:29092

COPY mock/target/*.jar app.jar
EXPOSE 8092
