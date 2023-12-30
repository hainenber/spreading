# Use an OpenJDK base image
FROM eclipse-temurin:21-jdk-ubi9-minimal

# Require specifying build JAR path
ARG JAR_PATH=spreading-0.0.1-SNAPSHOT.jar

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container at /app
COPY build/libs/${JAR_PATH} /app/app.jar

# Expose the port that your Spring Boot application will run on
EXPOSE 8080

# Command to run your application
CMD ["java", "-jar", "app.jar"]