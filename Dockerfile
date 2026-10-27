FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode clean package

FROM eclipse-temurin:17-jre

WORKDIR /app
RUN useradd --system --uid 10001 --create-home appuser
COPY --from=build --chown=appuser:appuser /workspace/target/student-management-*.jar /app/app.jar
USER appuser

EXPOSE 8089
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
