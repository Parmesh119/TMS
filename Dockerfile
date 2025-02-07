FROM openjdk:21-jdk-slim

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy Maven configuration
COPY pom.xml ./

# Download dependencies for offline use
RUN mvn dependency:go-offline

# Copy project files
COPY . .

# Build the application, skipping tests
RUN mvn clean package -DskipTests

# Expose application port
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=production

# Run the application
CMD ["java", "-jar", "target/TMS.jar"]
