FROM maven:3.8.6-eclipse-temurin-19-alpine as builder

COPY /src /app/src

COPY pom.xml /app

RUN mvn -f /app/pom.xml clean package

FROM openjdk:19-jdk-alpine3.16

COPY --from=builder ./app/target /app

WORKDIR /app

CMD [ "java", "-jar", "final-0.0.1-SNAPSHOT.jar" ]