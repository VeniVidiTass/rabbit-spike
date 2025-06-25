# Multi-Module Messaging Demo

This repository demonstrates a full RabbitMQ-based notification pipeline, featuring shared domain models, producers, consumers, and a MongoDB-to-RabbitMQ bridge.

## Modules

| Module                   | Purpose                                                                                       |
| ------------------------ | --------------------------------------------------------------------------------------------- |
| **shared/**              | Shared `Email` and `Sms` classes with MongoDB annotations.                                    |
| **consumer-email/**      | Listens on `email-queue` and sends HTML emails via SMTP (MailDev).                            |
| **consumer-sms/**        | Listens on `sms-queue` and simulates SMS delivery.                                            |
| **mongo-rabbit-bridge/** | Watches a MongoDB `appointments` collection and publishes enriched notifications to RabbitMQ. |

> **Note:** The `producer/` module is maintained separately.

## Usage with Docker Compose

A pre-configured **docker-compose.yml** is included. After cloning, simply:

```bash
docker-compose up -d
```

This brings up all services on the following ports:

| Service                   | Description                          | Port      |
| ------------------------- | ------------------------------------ | --------- |
| **RabbitMQ (management)** | AMQP broker + management UI          | 8083      |
| **Mongo Express**         | MongoDB web admin                    | 8081      |
| **MailDev UI**            | SMTP testing UI                      | 8082      |
| **consumer-email**        | Email consumer service               | (no HTTP) |
| **consumer-sms**          | SMS consumer service                 | (no HTTP) |
| **mongo-rabbit-bridge**   | Bridge service (background, no HTTP) | (no HTTP) |

Once up, you can:

1. Insert appointment docs into MongoDB.
2. Publish messages via any producer or directly to RabbitMQ.
3. View emails in MailDev UI at `http://localhost:8082`.
4. Explore RabbitMQ at `http://localhost:8083` (guest/guest).
5. Browse Mongo Express at `http://localhost:8081` (admin/pass).

## Building Locally

To compile all modules without Docker:

```bash
./mvnw clean install -DskipTests
```

Each module has its own README with detailed run instructions.
