# consumer-sms

A Spring Boot application that listens for `Sms` messages on a RabbitMQ queue and processes them via a simulated SMS service.

## Features

* Consumes JSON-serialized `Sms` objects from RabbitMQ (`sms-queue`).
* Supports manual acknowledgment with retry on failure.
* Simulates SMS sending latency and logs delivery status.
* Dockerized with multi-stage build and shared library support.

## Prerequisites

* Java 17
* Maven 3.8+ (or use the included Maven Wrapper)
* RabbitMQ server
* Docker (optional, for containerization)

## Configuration

All settings are located in `src/main/resources/application.properties`:

```properties
spring.application.name=consumer-sms
server.port=8082

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Retry settings
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.initial-interval=1000
spring.rabbitmq.listener.simple.retry.max-attempts=3
spring.rabbitmq.listener.simple.retry.max-interval=10000
spring.rabbitmq.listener.simple.retry.multiplier=2.0

# Logging
logging.level.com.example.consumersms=INFO
logging.level.org.springframework.amqp=INFO

# Exclude unused auto-configurations
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
```

Adjust host, ports, and credentials to match your environment.

## Build & Run

### Using Maven Wrapper

```bash
# Build (skips tests)
./mvnw clean package -DskipTests

# Run
java -jar target/consumer-sms-0.0.1-SNAPSHOT.jar
```

### Using Docker

```bash
# Build the Docker image
docker build -t consumer-sms .

# Run with Docker Compose (example)
docker-compose up -d rabbitmq consumer-sms
```

## Usage

1. Publish an `Sms` message to the `sms-queue` in RabbitMQ. The `Sms` model must match `com.example.shared.Sms`.
2. The application will process the message, simulate a delay/failure, and log the result.
3. Successful deliveries are acknowledged; failures are requeued for retry.
