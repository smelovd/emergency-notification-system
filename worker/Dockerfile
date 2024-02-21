FROM openjdk:21
COPY target/worker-0.0.1-SNAPSHOT.jar /app/worker.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "worker.jar"]