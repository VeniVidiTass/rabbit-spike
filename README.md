## 📬 RabbitMQ Spike — Producer & Consumer

This project is a minimal RabbitMQ messaging demo with two Spring Boot services:

- `producer`: Web app on `localhost:8080` with a form to send messages into a queue.
- `consumer`: Simulates an SMTP email sender by consuming messages from the queue and logging them.

The consumer introduces:
- Random processing delays (0–3 seconds)
- 33% chance to fail and requeue the message (simulating transient failures)

Useful for testing asynchronous message handling and retry behavior.

## 🚀 How to Run

Make sure you have [Docker](https://www.docker.com/products/docker-desktop) installed, then from the root of the project:

```bash
docker-compose up --build
```

### Access:

- Producer form: [http://localhost:8080](http://localhost:8080)
- RabbitMQ dashboard: [http://localhost:15672](http://localhost:15672)  
  (username: `guest`, password: `guest`)
- Consumer logs: visible in the terminal output
