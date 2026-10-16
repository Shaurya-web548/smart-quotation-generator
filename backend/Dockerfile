# Multi-stage Docker build for Smart Quotation Generator Backend
# Stage 1: Build with Maven + OpenJDK 21 (Maven is pre-installed in this image)
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom.xml first and download dependencies (layer-cached separately)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the fat JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal runtime image (no build tools, smaller size)
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the compiled fat JAR from builder stage
COPY --from=builder /build/target/smart-quotation-backend-1.0.0.jar app.jar

RUN chown -R spring:spring /app
USER spring:spring

ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${PORT}/api/v1/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
