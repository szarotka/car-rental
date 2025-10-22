# Multi-stage Dockerfile: build the Spring Boot jar and run it on OpenJDK 25
# Build stage
FROM openjdk:25-jdk AS build
WORKDIR /workspace
COPY . .
# Use Gradle wrapper to build the jar; skip tests to speed up image build
RUN ./gradlew bootJar -x test --no-daemon

# Run stage
FROM openjdk:25-jdk
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

