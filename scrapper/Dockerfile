# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app

COPY pom.xml .
COPY bot/pom.xml bot/
COPY scrapper/pom.xml scrapper/
COPY ai-agent/pom.xml ai-agent/
COPY contracts/pom.xml contracts/
COPY build-report-aggregate/pom.xml build-report-aggregate/
COPY migrations migrations

RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -pl scrapper -am

COPY contracts/src contracts/src
COPY scrapper/src scrapper/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -pl scrapper -am -DskipTests


FROM eclipse-temurin:25-jre-alpine AS extractor
WORKDIR /app
COPY --from=builder /app/scrapper/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S scrappergroup && adduser -S scrapperuser -G scrappergroup
USER scrapperuser

COPY --from=extractor /app/dependencies/ ./
COPY --from=extractor /app/spring-boot-loader/ ./
COPY --from=extractor /app/snapshot-dependencies/ ./
COPY --from=extractor /app/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
