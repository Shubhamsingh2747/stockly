# Stockly

Inventory and orders REST API (Spring Boot 3) plus a thin React UI.

## Stack

- Java 17, Spring Boot 3.4, Maven
- PostgreSQL + Flyway
- Spring Security + JWT
- Redis cache when Redis is running (in-memory fallback otherwise)
- React + TypeScript + Vite (`web/`)

## Prerequisites

- Java 17
- Maven
- PostgreSQL (create database `stockly`)
- Node.js 20+ (for the UI)
- Redis optional — install later if you want shared cache (`localhost:6379`)

Default local DB credentials in `api/src/main/resources/application-local.yml`:

- URL: `jdbc:postgresql://localhost:5432/stockly`
- User / password: `postgres` / `postgres`

Override with `SPRING_DATASOURCE_*` if yours differ.

## Run the API

```bash
cd api
mvn spring-boot:run
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

Demo admin (seeded on first start):

- Email: `admin@stockly.local`
- Password: `Admin@123`

## Run the UI

```bash
cd web
npm install
npm run dev
```

UI: http://localhost:5173 (CORS is enabled for this origin on the `local` profile).

## Main APIs

| Area | Endpoints |
| --- | --- |
| Auth | `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `GET /api/v1/auth/me` |
| Categories | `GET/POST /api/v1/categories` (`POST` is `ADMIN`) |
| Products | `GET/POST/PUT /api/v1/products`, filters `sku`, `category`, `lowStock` |
| Stock | `POST /api/v1/products/{id}/adjust-stock` (`ADMIN`), `GET /api/v1/products/{id}/movements` |
| Sales | `POST /api/v1/sales-orders`, `POST .../{id}/confirm`, `POST .../{id}/cancel` |
| Purchases | `POST /api/v1/purchase-orders`, `POST .../{id}/receive`, `POST .../{id}/cancel` |

Confirming a sales order decrements stock in one transaction (optimistic locking on product `version`). Receiving a purchase order increments stock. Low-stock events go through `InventoryEventPublisher` (logging today; Kafka later on topic `inventory.events`).

## Tests

```bash
cd api
mvn test
```

## Deploy later

Use profile `prod` and env vars from [`.env.example`](.env.example) (`SPRING_DATASOURCE_*`, `JWT_SECRET`, `STOCKLY_CORS_ORIGINS`). No Docker in this project.

## Layout

```
api/   Spring Boot service
web/   Vite React app
```
