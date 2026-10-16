# Multi-stage Docker build for Smart Quotation Generator Backend
# Stage 1: Build stage with Maven and OpenJDK 21
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

# Copy pom.xml and download dependencies first for fast layer caching
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B 2>/dev/null || mvn dependency:go-offline -B 2>/dev/null || true

# Copy source code and build production fat JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage with minimal JRE
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create non-root system user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy compiled JAR from builder stage
COPY --from=builder /build/target/smart-quotation-backend-1.0.0.jar app.jar

# Set ownership
RUN chown -R spring:spring /app

USER spring:spring

ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:${PORT}/api/v1/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
