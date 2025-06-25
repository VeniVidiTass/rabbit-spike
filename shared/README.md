# shared

A Maven module providing shared domain models (`Email` and `Sms`) for inter-service communication.

## Overview

This library contains:

* **Email.java**: Defines the `Email` entity with fields: `id`, `from`, `to`, `subject`, `body`, and `scheduledAt`.
* **Sms.java**: Defines the `Sms` entity with fields: `id`, `from`, `to`, `body`, and `scheduledAt`.

Both classes include MongoDB annotations for document persistence and support form-binding via `getText`/`setText` methods.

## Module Structure

```
shared/
└── pom.xml
└── src/
    └── main/
        └── java/com/example/shared/
            ├── Email.java
            └── Sms.java
```

## pom.xml Configuration

* Parent: Spring Boot Starter Parent 3.5.0
* Java version: 17
* Dependency: `spring-boot-starter-data-mongodb` for annotation support

## Usage

Add this dependency to your project's `pom.xml`:

```xml
<dependency>
  <groupId>com.example</groupId>
  <artifactId>shared</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Then import and use the models:

```java
import com.example.shared.Email;
import com.example.shared.Sms;
```

## Persistence

These entities are annotated with Spring Data MongoDB:

```java
@Document(collection = "email")
public class Email { /* ... */ }

@Document(collection = "sms")
public class Sms { /* ... */ }
```

Configure MongoDB connection in your application to persist and retrieve messages.
