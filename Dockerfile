FROM openjdk:23

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/POS-0.0.1-SNAPSHOT.jar /app

# Expose the port that the Spring Boot application will run on
EXPOSE 8082

# Command to run the application
CMD ["java", "-jar", "POS-0.0.1-SNAPSHOT.jar"]
