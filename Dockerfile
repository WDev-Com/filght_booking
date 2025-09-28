# 1️⃣ Build Stage
FROM maven:3.9.6-eclipse-temurin-17 as builder
WORKDIR /app

# Copy pom.xml first (for dependency caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# 2️⃣ Runtime Stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
