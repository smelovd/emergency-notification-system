Notification System
-

Navigation
----------
* [Stack](https://github.com/smelovd/notification-api/tree/main#stack)
* [Application architecture](https://github.com/smelovd/notification-api/tree/main#application-architecture)
* [How to run?](https://github.com/smelovd/notification-api/tree/main#how-to-run)

Stack
------
Spring Framework: Boot, Data; Mongo, Kafka, Redis, Maven, Docker

Application architecture
-----------------------------
![image](https://github.com/smelovd/notification-api/assets/102801923/7247cc66-2724-4ef8-93a9-b58b0c0c67cc)


How to run?
-----------------------------------------------------------------------
You can just run this `docker-compose.yml`, 

```yml
version: '3'
services:
  api:
    image: bdhb8g6ed8c/notification-api
    container_name: "api"
    depends_on:
      - mongodb
      - kafka
    ports:
      - "8080:8080"
    links:
      - mongodb
      - kafka
  worker:
    image: bdhb8g6ed8c/notification-worker
    #container_name: "worker"
    depends_on:
      - redis
      - mongodb
      - kafka
    links:
      - redis
      - mongodb
      - kafka
    deploy:
      mode: replicated
      replicas: 2
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    container_name: "zookeeper"
    restart: always
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
  kafka:
    image: confluentinc/cp-kafka:latest
    container_name: "kafka"
    depends_on:
      - zookeeper
    restart: always
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    healthcheck:
      test: ["CMD", "kafka-topics", "--bootstrap-server", "kafka:9092", "--list"]
      interval: 30s
      timeout: 10s
      retries: 10
  mongodb:
    image: mongo:4.4.6
    container_name: "mongodb"
    restart: always
    ports:
      - "27017:27017"
  redis:
    image: redis:latest
    container_name: "redis"
    restart: always
    ports:
      - "6379:6379"
```

Usage:
------
