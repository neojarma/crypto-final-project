FROM openjdk:19-jdk-alpine3.16

RUN mkdir /app

COPY ./target/final-0.0.1-SNAPSHOT.jar /app

WORKDIR /app

CMD [ "java", "-jar", "final-0.0.1-SNAPSHOT.jar" ]