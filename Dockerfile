FROM ubuntu:latest
LABEL authors="Jie Kin"

ENTRYPOINT ["top", "-b"]

FROM eclipse-temurin:17-jdk-alpine
COPY build/libs/wealthtech-ledger-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]