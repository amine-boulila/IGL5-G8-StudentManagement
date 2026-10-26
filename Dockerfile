# Image de l'application Spring Boot (le .jar est construit par Maven avant le docker build)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Utilisateur non-root (bonne pratique de securite)
RUN addgroup -S spring && adduser -S spring -G spring

COPY target/*.jar app.jar
RUN chown spring:spring app.jar
USER spring

# server.port=8089, context-path=/student (voir application.properties)
EXPOSE 8089

# La base MySQL se configure par variables d'environnement au lancement :
# SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
