# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app

COPY pom.xml .
COPY ai-agent/pom.xml ai-agent/
COPY bot/pom.xml bot/
COPY contracts/pom.xml contracts/
COPY scrapper/pom.xml scrapper/
COPY build-report-aggregate/pom.xml build-report-aggregate/

RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -pl contracts -am

COPY contracts/src contracts/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -pl contracts -am -DskipTests

FROM alpine:3.21
WORKDIR /out
COPY --from=builder /app/contracts/target/*.jar ./
