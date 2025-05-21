# 📦 Product Order Service

A scalable Spring Boot REST API for managing **products** and **orders** with real-time stock reservation, expiration handling (30 minutes), and Kafka-based messaging.

---

## 🚀 Features

- ✅ Create, update, delete, and list products
- ✅ Create, cancel, and pay for orders
- ✅ Stock is reserved immediately upon order creation
- ✅ Orders expire if not paid within 30 minutes (Kafka-driven)
- ✅ Validation error handling with detailed messages
- ✅ Spring REST Docs and Swagger UI for documentation
- ✅ Dockerized infrastructure for local development

---

## ⚙️ Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA + H2
- Liquibase
- Apache Kafka + Zookeeper
- Spring REST Docs
- Springdoc OpenAPI (Swagger)
- Docker + Docker Compose
- Lombok

---

## 🐳 Docker Setup

### Build and Run the Project

```bash
docker-compose down -v --remove-orphans
docker-compose up --build
```

---

## 🌐 Available Services

| Service              | URL                                 |
|----------------------|--------------------------------------|
| Product Order API    | http://localhost:8080/swagger-ui.html |
| Kafka UI             | http://localhost:8081               |
| Health Check         | http://localhost:8080/actuator/health |

---

## 📖 API Documentation

### ✅ Swagger UI

Interactive OpenAPI explorer:  
➡️ `http://localhost:8080/swagger-ui.html`

### ✅ Spring REST Docs (HTML)

After running:

```bash
mvn clean verify
```

Open generated docs:

```bash
target/generated-docs/index.html
```

---

## 🧪 Running Tests

```bash
mvn clean verify
```

Includes:
- ✅ Unit tests
- ✅ REST Docs generation
- ✅ Kafka producer/consumer tests

---

## 🧼 Cleanup

Stop all containers and clean volumes:

```bash
docker-compose down -v --remove-orphans
```

---

## 📂 Project Structure

```
src/
├── main/
│   ├── java/com/vserdiuk/casestudy/
│   │   ├── CasestudyApplication.java
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── mapper/
│   │   ├── messaging/
│   │   ├── repository/
│   │   └── service/
│   │       ├── impl/
│   │       └── interface/
│   ├── resources/
│   │   ├── application.yml
│   │   └── db/changelog/
├── test/
│   └── java/com/vserdiuk/casestudy/
│   │   ├── controller/
│   │   ├── repository/
│   │   └── service/

```

---
