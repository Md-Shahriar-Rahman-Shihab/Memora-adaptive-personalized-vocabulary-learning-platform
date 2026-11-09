# ==============================================================================
# Memora Backend - Production Multi-Stage Dockerfile
# Stage 1: Build JAR using Maven and OpenJDK 21 LTS
# Stage 2: Minimal, secure JRE 21 runtime running as non-root user
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build stage
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

# Copy Maven wrapper and POM first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Normalize line endings and grant execution permission
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Pre-fetch project dependencies for fast cached rebuilds
RUN ./mvnw dependency:go-offline -B || true

# Copy backend source code
COPY src ./src

# Build production executable JAR (tests verified pre-deployment)
RUN ./mvnw clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Stage 2: Production Runtime Stage
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

# Install curl for container health check
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Create non-root system group and user
RUN groupadd -r memora && useradd -r -g memora -d /app -s /sbin/nologin memora

WORKDIR /app

# Copy executable JAR from builder stage
COPY --from=builder /build/target/*.jar /app/app.jar

# Set strict file ownership
RUN chown -R memora:memora /app

# Switch to non-privileged user
USER memora

# Production environment defaults
# PORT is dynamically assigned by Render; safe local default is 8080.
# JAVA_OPTS tunes container memory allocation:
# - MaxRAMPercentage=70.0 allocates up to 70% of container RAM to heap
# - Min/InitialRAMPercentage=50.0 ensures predictable initial memory
# - Prefer IPv4 stack ensures reliable network resolution on cloud hosts
ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70.0 -XX:MinRAMPercentage=50.0 -XX:InitialRAMPercentage=50.0 -XX:+ExitOnOutOfMemoryError -Djava.net.preferIPv4Stack=true"

# Inform Docker that the container listens on the default port
EXPOSE 8080

# Health check instruction using Memora's unauthenticated health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD curl -f http://127.0.0.1:${PORT}/api/v1/health || exit 1

# Execute Spring Boot using exec so Java is PID 1 and receives OS shutdown signals
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

