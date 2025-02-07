FROM openjdk:21-jdk-slim

WORKDIR /app

COPY pom.xml ./

RUN mvn dependency:go-offline

COPY . .

RUN mvn clean package -DskipTests

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=production

CMD ["java", "-jar", "target/TMS.jar"]