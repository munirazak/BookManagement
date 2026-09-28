# Multi-stage build: compile with Maven, run on a slim JRE image
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd -r app && useradd -r -g app app \
    && mkdir -p /app/logs \
    && chown -R app:app /app

COPY --from=build --chown=app:app /app/target/BookManagement-0.0.1-SNAPSHOT.jar app.jar

USER app

# Default local/Docker port. GCP Cloud Run injects PORT at runtime.
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
