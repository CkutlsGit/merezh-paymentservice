# Payment Service - Merezh

Microservice responsible for processing payments for orders.

📖 In Russian: [перевод на русский](#)

## 📋 Overview

Payment Service is a Spring Boot microservice that manages the payment lifecycle: creating a payment for an order, debiting funds from the user's wallet, and notifying Order Service about status changes.

The service stores **payment history** and participates in a **saga**: `order → payment → wallet → payment → order`. It uses `@Transactional(noRollbackFor = ...)` so that on an HTTP-call error the payment status is still persisted (`FAILED` on 4xx, `WAITING` on 5xx), and notifies Order Service via a callback in `finally`.

## 🚀 Technology Stack

**Backend**

- Java 21 - core language
- Spring Boot 3 - application framework
- Spring Data JPA - database access and ORM
- RestTemplate - synchronous HTTP calls to Wallet Service and Order Service

**Database**

- PostgreSQL - production database

**DevOps**

- Docker - containerization
- Docker Compose - multi-container orchestration
- Spring Boot Actuator - health checks and monitoring

## ✨ Features

### 💳 Payment Management

- Create a payment for an order (`placeOrder`) - called by Order Service
- Pay for an order (`payOrder`) - called by the user through the Gateway
- Get payment by orderId
- Get all payments for a user
- Idempotency by `orderId` (one order - one payment)

### 🔄 Saga with Order Service and Wallet Service

- **Order Service → Payment Service**: `POST /place` on order creation
- **Payment Service → Wallet Service**: `POST /balance/sub` on payment
- **Payment Service → Order Service**: `POST /update` with the new status
- On 4xx from Wallet - payment is marked `FAILED`
- On 5xx from Wallet - payment is marked `WAITING` (for retry)
- Callback to Order Service is **always** executed via `finally`

### 🔒 Reliability

- `@Transactional(noRollbackFor = {HttpClientErrorException, HttpServerErrorException})` - status is persisted even on a network error
- Repeated `/place` for an existing payment triggers `existsOrder` (retry of the debit if status is `WAITING`)
- `Payment` uses `orderId` as the primary key - protection against duplicates

### ✅ Data Validation

- Consistent error responses via `@RestControllerAdvice`
- Error propagation from Wallet and Order (forwards `message` from the response)

## 🛠️ Quick Start

### Prerequisites

- Docker
- Docker Compose

### Run with Docker Compose

```bash
docker compose up --build
```

The service will be available on port **8084**. 
Swagger path - `/swagger-ui.html`.

## 📚 API Endpoints

Base path: `/api/v1/payments`

| Method | Endpoint           | Description                               | Access                     |
|--------|--------------------|-------------------------------------------|----------------------------|
| GET    | `/get/{id}`        | Get payment by orderId                    | Authenticated              |
| GET    | `/get/user`        | Get payments for the user                 | Authenticated              |
| POST   | `/place`           | Create a payment (called by Order Service)| Internal (order-service)   |
| POST   | `/pay/{orderId}`   | Pay for an order                          | Authenticated              |

**Note:** Protected endpoints expect the `X-User-Id` header, which is set by the Gateway.

## 📦 Project Structure

```
src/main/java/ru/merezh/paymentservice/
├── config/                    # Spring configuration (RestTemplate)
├── controller/                # REST controllers
├── dto/                       # Data Transfer Objects
├── entity/                    # JPA entities (Payment) + PaymentStatus enum
├── exception/                 # Custom exceptions and handlers
│   ├── controller/            # @RestControllerAdvice
│   └── dto/                   # Error response DTOs
├── repository/                # Spring Data JPA repositories
└── service/                   # Business logic (PaymentService)
```

## 🔒 Security and Reliability

- **A user can only pay for their own order** - check `payment.getUserId() != userId` → `400`.
- **Idempotency**: repeated payment of an already-paid order returns the current status without re-debiting.
- **`noRollbackFor`** - the payment status is persisted even on an HTTP error.
- **Callback to Order Service** - always, via `finally`.
- **The service trusts the Gateway** for user identity.
- **The internal `/place` endpoint** must not be publicly exposed - only for Order Service.

## 🩺 Health Checks

The service exposes Spring Boot Actuator endpoints:

| Endpoint                     | Purpose                        |
|------------------------------|--------------------------------|
| `/actuator/health`           | Overall health                 |
| `/actuator/health/liveness`  | Liveness probe                 |
| `/actuator/health/readiness` | Readiness probe (includes DB)  |
| `/actuator/info`             | Service info                   |
