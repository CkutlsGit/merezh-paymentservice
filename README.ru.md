# Payment Service - Merezh

Микросервис, отвечающий за обработку платежей по заказам.

## 📋 Обзор

Payment Service - это микросервис на Spring Boot, который управляет жизненным циклом платежа: создание платежа по заказу, списание средств с кошелька пользователя и уведомление Order Service об изменении статуса.

Сервис хранит **историю платежей** и работает в **саге**: `order → payment → wallet → payment → order`. Он использует `@Transactional(noRollbackFor = ...)`, чтобы при ошибке HTTP-вызова статус платежа сохранялся (`FAILED` при 4xx, `WAITING` при 5xx), и уведомляет Order Service через callback в `finally`.

## 🚀 Технологический стек

**Backend**

- Java 21 - основной язык
- Spring Boot 3 - фреймворк приложения
- Spring Data JPA - доступ к БД и ORM
- RestTemplate - синхронные HTTP-вызовы к Wallet Service и Order Service

**База данных**

- PostgreSQL - основная БД

**DevOps**

- Docker - контейнеризация
- Docker Compose - оркестрация нескольких контейнеров
- Spring Boot Actuator - healthcheck и мониторинг

## ✨ Возможности

### 💳 Управление платежами

- Создание платежа по заказу (`placeOrder`) - вызывается Order Service
- Оплата заказа (`payOrder`) - вызывается пользователем через Gateway
- Получение платежа по orderId
- Получение всех платежей пользователя
- Идемпотентность по `orderId` (один заказ - один платёж)

### 🔄 Сага с Order Service и Wallet Service

- **Order Service → Payment Service**: `POST /place` при создании заказа
- **Payment Service → Wallet Service**: `POST /balance/sub` при оплате
- **Payment Service → Order Service**: `POST /update` с новым статусом
- При 4xx от Wallet - платёж помечается `FAILED`
- При 5xx от Wallet - платёж помечается `WAITING` (для повторной попытки)
- Callback в Order Service выполняется **всегда** через `finally`

### 🔒 Надёжность

- `@Transactional(noRollbackFor = {HttpClientErrorException, HttpServerErrorException})` - статус фиксируется даже при сетевой ошибке
- Повторный `/place` для существующего платежа вызывает `existsOrder` (повторная попытка списания, если статус `WAITING`)
- `Payment` использует `orderId` как первичный ключ - защита от дублей

### ✅ Валидация данных

- Единый формат ошибок через `@RestControllerAdvice`
- Обработка ошибок от Wallet и Order (проброс `message` из ответа)

## 🛠️ Быстрый старт

### Требования

- Docker
- Docker Compose

### Запуск через Docker Compose

```bash
docker compose up --build
```

Сервис будет доступен на порту **8084**.
Swagger path - `/swagger-ui.html`.

## 📚 Эндпоинты API

Базовый путь: `/api/v1/payments`

| Метод | Эндпоинт           | Описание                                  | Доступ                     |
|-------|--------------------|-------------------------------------------|----------------------------|
| GET   | `/get/{id}`        | Получить платёж по orderId                | Authenticated              |
| GET   | `/get/user`        | Получить платежи пользователя             | Authenticated              |
| POST  | `/place`           | Создать платёж (вызывается Order Service) | Internal (order-service)   |
| POST  | `/pay/{orderId}`   | Оплатить заказ                            | Authenticated              |

**Примечание:** Защищённые эндпоинты ожидают заголовок `X-User-Id`, который устанавливает Gateway.

## 📦 Структура проекта

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

## 🔒 Безопасность и надёжность

- **Пользователь может оплатить только свой заказ** - проверка `payment.getUserId() != userId` → `400`.
- **Идемпотентность**: повторная оплата уже оплаченного заказа возвращает текущий статус без повторного списания.
- **`noRollbackFor`** - статус платежа сохраняется даже при ошибке HTTP-вызова.
- **Callback в Order Service** - всегда, через `finally`.
- **Сервис доверяет Gateway** в вопросе идентификации пользователя.
- **Внутренний эндпоинт `/place`** не должен публиковаться напрямую - только для Order Service.

## 🩺 Health Checks

Сервис предоставляет эндпоинты Spring Boot Actuator:

| Эндпоинт                     | Назначение                   |
|------------------------------|------------------------------|
| `/actuator/health`           | Общий статус                 |
| `/actuator/health/liveness`  | Liveness probe               |
| `/actuator/health/readiness` | Readiness probe (включая БД) |
| `/actuator/info`             | Информация о сервисе         |
