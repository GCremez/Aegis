# Use official OpenJDK 21 runtime as a parent image
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper files
COPY .mvn .mvn
COPY mvnw .

# Make mvnw executable (important for Unix systems)
RUN chmod +x mvnw

# Copy pom.xml first to leverage Docker layer caching
COPY pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Runtime stage - smaller image
FROM eclipse-temurin:21-jre-alpine

# Add curl for health checks
RUN apk add --no-cache curl

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# JVM tuning for high-performance, low-latency
ENV JAVA_OPTS="-XX:+UseZGC \
               -XX:+ZGenerational \
               -Xms512m \
               -Xmx2g \
               -XX:MaxGCPauseMillis=1 \
               -XX:+AlwaysPreTouch \
               -XX:+UseStringDeduplication \
               -Djava.security.egd=file:/dev/./urandom"

# Expose port (Render will provide PORT env variable)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application with proper port binding
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]