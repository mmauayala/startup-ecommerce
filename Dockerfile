# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

COPY v1/pom.xml v1/pom.xml

RUN ./mvnw -q -f v1/pom.xml -DskipTests dependency:go-offline
COPY v1/src v1/src

RUN ./mvnw -q -e -f v1/pom.xml -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV JAVA_OPTS=""

COPY --from=builder /workspace/v1/target/*.jar /app/app.jar

VOLUME ["/uploads"]

EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]


