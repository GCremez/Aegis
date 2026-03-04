# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# JVM tuning for FREE TIER (512MB limit)
ENV JAVA_OPTS="-XX:+UseSerialGC \
               -Xms128m \
               -Xmx350m \
               -XX:MaxMetaspaceSize=128m \
               -XX:+TieredCompilation \
               -XX:TieredStopAtLevel=1 \
               -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]