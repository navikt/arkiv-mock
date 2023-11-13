FROM gcr.io/distroless/java21-debian12:nonroot

ENV SPRING_PROFILES_ACTIVE=docker
ENV KAFKA_BROKERS=kafka-broker:29092
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"

COPY mock/target/*.jar app.jar
EXPOSE 8092
