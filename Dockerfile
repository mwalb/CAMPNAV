# Multi-stage Docker build for CAMPNAV Spring Boot Backend (Repo root context)
# Stage 1: Build stage
FROM docker.io/eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Gradle wrapper and configuration files from backend/
COPY backend/gradlew backend/
COPY backend/gradle backend/gradle
COPY backend/build.gradle backend/settings.gradle backend/

WORKDIR /app/backend

# Grant execution rights to gradlew
RUN chmod +x gradlew

# Pre-fetch project dependencies
RUN ./gradlew dependencies --no-daemon || true

# Copy backend source code
COPY backend/src src

# Build executable boot JAR
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Production runtime stage
FROM docker.io/eclipse-temurin:21-jre-alpine

WORKDIR /app

# Run container process as a unprivileged non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy executable JAR from builder stage
COPY --from=builder /app/backend/build/libs/*.jar app.jar

# Adjust ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

EXPOSE 8080

# Environment defaults appropriate for 512MB RAM container
ENV PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0 -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
