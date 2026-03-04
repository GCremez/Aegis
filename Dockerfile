# Use official OpenJDK 21 runtime as a parent image
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper files
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

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
  CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

## Key Changes Made:

1. **Line 8-10**: Fixed COPY commands to properly copy `.mvn` directory
2. **Line 13**: Added `chmod +x mvnw` to make it executable
3. **Line 16**: Copy `pom.xml` separately for better caching
4. **Line 19**: Added `-B` flag for batch mode (cleaner logs)
5. **Line 38**: Fixed JAR copy to use wildcard pattern

## Update Your `.dockerignore`

Make sure your `.dockerignore` looks like this:
```
# Build artifacts
target/
*.class

# IDE files
.idea/
*.iml
.vscode/
.settings/
.project
.classpath

# OS files
.DS_Store
Thumbs.db

# Git (but keep .mvn)
.git/

# Logs
*.log
logs/

# Test output
test-output/