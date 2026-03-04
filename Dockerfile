# Use official OpenJDK 21 runtime as a parent image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first to leverage Docker layer caching
COPY mvnw mvnw.cmd .mvn/wrapper/maven-wrapper.jar .mvn/wrapper/maven-wrapper.properties pom.xml ./

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/Aegis-0.0.1-SNAPSHOT.jar"]
