# ------------------------------------------------------------------------------
# Build Stage 1: Build the Maven artifact
# ------------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and resolve dependencies to leverage Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application (skip tests in docker build for speed)
COPY src ./src
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------
# Build Stage 2: Minimal Distroless / Temurin Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add non-root user for enterprise container security
RUN addgroup -S auditrail && adduser -S auditrail -G auditrail
USER auditrail:auditrail

# Copy built executable jar from builder stage
COPY --from=builder /app/target/auditrail-backend-1.0.0.jar app.jar

# Expose backend port
EXPOSE 8080

# Configure memory constraints and health checks
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
