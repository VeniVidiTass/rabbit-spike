# mongo-rabbit-bridge

A Spring Boot background service that bridges MongoDB appointment records to RabbitMQ-based email and SMS notifications. It watches a MongoDB collection for new appointments, enriches them via external HTTP APIs, and publishes formatted `Email` and `Sms` messages to the appropriate RabbitMQ queues.

## Features

* **Change Stream Listener**: Uses Spring Data MongoDB change streams to react to **insert** events in the configured appointments collection.
* **DTO Extraction**: Maps raw BSON documents into immutable `AppointmentDto` objects, with safe handling of missing email or phone fields.
* **External Enrichment**: Fetches doctor and service details via reactive `WebClient` clients (`DoctorApiClient` and `ServiceApiClient`), each with:

    * 3‑second timeout
    * Fallback to raw ID on non-2xx or error
* **Template‑Driven Email**: Renders an HTML appointment confirmation email using Thymeleaf and `appointment-confirmation.html`, packaging it into a `com.example.shared.Email` object.
* **Plain‑Text SMS**: Constructs a concise SMS message string with appointment code, date/time, service, and doctor, wrapped in a `com.example.shared.Sms` object.
* **RabbitMQ Publishing**: Sends JSON‑serialized `Email` and `Sms` messages to the queues defined by `app.bridge.email-queue` and `app.bridge.sms-queue`, using a Jackson message converter that trusts only `com.example.shared`.
* **Zero Web Footprint**: Runs with `spring.main.web-application-type=none`, exposing no HTTP endpoints—ideal for sidecar or worker deployment.

## Architecture Overview

```text
MongoDB (appointments) ──▶ ChangeStreamListener ──▶ AppointmentDto
                                       │
                          ┌────────────┴────────────┐
                          │                         │
                DoctorApiClient                ServiceApiClient
                          │                         │
                          └────────────┬────────────┘
                                       │
         ┌─────────────────────────────┴─────────────────────────────┐
         │                                                           │
  EmailSenderService                                       SmsSenderService
         │                                                           │
  AppointmentToEmailMapper                                     (plain-text)
         │                                                           │
  RabbitTemplate (Email Queue)                          RabbitTemplate (SMS Queue)
```

1. **ChangeStreamListener** watches the `app.bridge.appointment-collection` for inserts.
2. Builds an **AppointmentDto**, throwing `MissingEmailException` if email is absent.
3. **DoctorApiClient** and **ServiceApiClient** fetch enrichment data with fallbacks.
4. **EmailSenderService** uses **AppointmentToEmailMapper** + Thymeleaf to create HTML email.
5. **SmsSenderService** formats a plain-text SMS string.
6. **RabbitTemplate** publishes to RabbitMQ queues with a secure JSON converter.

## Prerequisites

* Java 17
* Maven 3.8+ (or use the included Maven Wrapper)
* MongoDB replica set (`appointments-db:27017`, `gestmed_appointments_db`, `rs0`)
* RabbitMQ server
* Downstream HTTP APIs for doctors and services
* Docker (optional, for containerization)

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Disable web server
spring.main.web-application-type=none

# MongoDB
spring.data.mongodb.uri=mongodb://appointments-db:27017/gestmed_appointments_db?replicaSet=rs0

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Appointment bridge settings
app.bridge.email-queue=email-queue
app.bridge.sms-queue=sms-queue
app.bridge.appointment-collection=appointments

# Downstream service base URL
app.bridge.service-api-base-url=http://api-gateway:3000/api

# Logging
logging.level.org.springframework.data.mongodb.core.messaging=DEBUG
logging.level.com.rabbit.bridge.mongorabbitbridge.listener=DEBUG
```

## Build & Run

### Using Maven Wrapper

```bash
# Build shared + bridge modules
./mvnw clean package -DskipTests

# Run
java -jar mongo-rabbit-bridge/target/mongo-rabbit-bridge-0.0.1-SNAPSHOT.jar
```

### Using Docker

```bash
# From project root
docker build -t mongo-rabbit-bridge -f mongo-rabbit-bridge/Dockerfile .

docker run -d \
  --name mongo-rabbit-bridge \
  --network your-network \
  -e SPRING_DATA_MONGODB_URI="mongodb://appointments-db:27017/..." \
  -e SPRING_RABBITMQ_HOST=rabbitmq \
  mongo-rabbit-bridge
```

## Templates

The only template, `appointment-confirmation.html`, lives under `src/main/resources/templates/`. It uses responsive inline styles and dark-mode support to produce polished confirmation emails.
