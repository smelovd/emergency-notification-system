FROM openjdk:21
COPY target/api-0.0.1-SNAPSHOT.jar /app/api.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "api.jar"]