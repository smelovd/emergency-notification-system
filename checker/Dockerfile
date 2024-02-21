FROM openjdk:21
COPY target/checker-0.0.1-SNAPSHOT.jar /app/checker.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "checker.jar"]