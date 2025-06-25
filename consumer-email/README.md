# consumer-email

A Spring Boot application that listens for `Email` messages on a RabbitMQ queue and forwards them via SMTP (MailDev).

## Features

* Consumes JSON-serialized `Email` objects from RabbitMQ (`email-queue`).
* Supports manual acknowledgment with retry on failure.
* Sends emails using Spring Boot's `JavaMailSender` to a MailDev SMTP server.
* Dockerized with multi-stage build and shared library support.

## Prerequisites

* Java 17
* Maven 3.8+ (or use the included Maven Wrapper)
* RabbitMQ server
* MailDev (or any SMTP server) for email testing
* Docker (optional, for containerization)

## Configuration

All settings are located in `src/main/resources/application.properties`:

```properties
spring.application.name=consumer
server.port=8081

# RabbitMQ
spring.rabbitmq.host=rabbitmq
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# SMTP (MailDev)
spring.mail.host=maildev
spring.mail.port=1025
spring.mail.protocol=smtp
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
```

Adjust hostnames and ports to match your environment.

## Build & Run

### Using Maven Wrapper

```bash
# Build (skips tests)
./mvnw clean package -DskipTests

# Run
java -jar target/consumer-0.0.1-SNAPSHOT.jar
```

### Using Docker

```bash
# Build the Docker image
docker build -t consumer-email .

# Run with Docker Compose (example)
docker-compose up -d rabbitmq maildev consumer-email
```

## Usage

1. Publish an `Email` message to the `email-queue` in RabbitMQ. The `Email` model must match `com.example.shared.Email`.
2. The application will process the message, simulate a delay/failure, and send the email via SMTP.
3. Successful deliveries are acknowledged; failures are requeued for retry.
