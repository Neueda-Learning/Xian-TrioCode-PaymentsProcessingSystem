FROM eclipse-temurin:17-jdk
# Set working directory
WORKDIR /app
# Copy the built JAR from the builder stage
COPY target/*.jar app.jar
# Expose the default Spring Boot port
EXPOSE 8080
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]