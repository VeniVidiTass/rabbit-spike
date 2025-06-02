## 📬 RabbitMQ Spike — Producer & Multiple Consumers

This project is a RabbitMQ messaging demo with Spring Boot services:

- `producer`: Web app on `localhost:8080` with forms to send both Email and SMS messages into separate queues.
- `consumer-email`: Dedicated consumer for email messages that simulates SMTP email sending via MailDev.
- `consumer-sms`: Dedicated consumer for SMS messages that simulates SMS sending via external providers.

The consumers introduce:
- Random processing delays (emails: 0–3 seconds, SMS: 0–1.5 seconds)
- Simulated failures with automatic requeue (emails: 33% failure, SMS: 15% failure)

Useful for testing asynchronous message handling, retry behavior, and multiple queue processing.

## 🚀 How to Run

Make sure you have [Docker](https://www.docker.com/products/docker-desktop) installed, then from the root of the project:

```bash
docker-compose up --build
```

### Access:

- **Producer forms**: [http://localhost:8080](http://localhost:8080)
  - Email form: [http://localhost:8080/](http://localhost:8080/)
  - SMS form: [http://localhost:8080/sms/](http://localhost:8080/sms/)
- **RabbitMQ dashboard**: [http://localhost:15672](http://localhost:15672)  
  (username: `guest`, password: `guest`)
- **MailDev (email testing)**: [http://localhost:8084](http://localhost:8084)
- **Mongo Express (database)**: [http://localhost:8083](http://localhost:8083)  
  (username: `admin`, password: `admin`)
- **Consumer Email**: Running on port 8081
- **Consumer SMS**: Running on port 8082
- Consumer logs: visible in the terminal output

### Services & Ports:

| Service | Port | Description |
|---------|------|-------------|
| Producer | 8080 | Web forms for sending messages |
| Consumer Email | 8081 | Email message processor |
| Consumer SMS | 8082 | SMS message processor |
| Mongo Express | 8083 | MongoDB web interface |
| MailDev | 8084 | Email testing interface |
| RabbitMQ Management | 15672 | Queue management interface |
| MongoDB | 27017 | Database |
| RabbitMQ | 5672 | Message broker |
