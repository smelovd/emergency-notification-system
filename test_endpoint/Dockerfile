FROM openjdk:21
COPY target/test_endpoint-0.0.1-SNAPSHOT.jar /app/test-endpoint.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "test-endpoint.jar"]