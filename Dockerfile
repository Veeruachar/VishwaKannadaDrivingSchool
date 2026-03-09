# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
COPY truststore.jks ./truststore.jks
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy assets from build stage
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/truststore.jks /app/truststore.jks

# Render provides the PORT env var; Spring Boot listens to it automatically
EXPOSE 8080

# Run with absolute path to the truststore
ENTRYPOINT ["java", "-Dserver.port=${PORT:8080}", "-jar", "app.jar"]