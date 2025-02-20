FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Image recommended on playwright website
FROM mcr.microsoft.com/playwright/java:v1.49.0-noble
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

