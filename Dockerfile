# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-slim
WORKDIR /app

# Add a non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Configure JVM options
ENV JAVA_OPTS="-XX:+UseG1GC -Xmx512m -Xms256m -XX:+UseContainerSupport"

EXPOSE 8080
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar