FROM openjdk:21-jdk-slim

WORKDIR /app

# Install Maven and netcat
RUN apt-get update && \
    apt-get install -y maven netcat-openbsd && \
    rm -rf /var/lib/apt/lists/*

# Copy Maven configuration
COPY pom.xml ./

# Download dependencies for offline use
RUN mvn dependency:go-offline

# Copy project files
COPY . .
COPY wait-for-it.sh /app/wait-for-it.sh

RUN chmod +x /app/wait-for-it.sh

# Create a simple wait script
RUN echo '#!/bin/sh\nwhile ! nc -z db 5432; do echo "Waiting for database..."; sleep 1; done; echo "Database is ready!"; exec java -jar target/TMS-0.0.1-SNAPSHOT.jar' > wait-and-start.sh && \
    chmod +x wait-and-start.sh

# Build the application, skipping tests
RUN mvn clean package -DskipTests

# Expose application port
EXPOSE 8081

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=production

# Use the simple wait script
CMD ["/app/wait-for-it.sh", "db:5432", "--", "java", "-jar", "target/TMS-0.0.1-SNAPSHOT.jar"]