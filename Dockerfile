# Use a base image with Java runtime
FROM openjdk:17-jdk-slim as builder

# Set working directory
WORKDIR /app

# Copy build artifacts (replace with your JAR path if different)
COPY target/*.jar app.jar

# Run the jar file
ENTRYPOINT ["java","-jar","/app/app.jar"]
