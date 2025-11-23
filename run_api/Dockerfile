FROM --platform=linux/amd64 eclipse-temurin:21-jdk

WORKDIR /app

ARG JAR_FILE=build/libs/run-api-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]