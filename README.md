# Identity & Access Management (IAM) Service

A robust, Multi-Tenant Identity Service built with **Java 25**, **Spring Boot 4**, and **Domain-Driven Design (DDD)** principles.

This project demonstrates a production-grade implementation of **Hexagonal Architecture (Ports & Adapters)**, **CQRS** (Command Query Responsibility Segregation), and **OAuth2/OIDC Security** using Keycloak.

## Key Features

* **Domain-Driven Design**: Pure Java domain layer isolated from infrastructure. Rich models (`Tenant`, `User`) with strict invariant enforcement.
* **Hexagonal Architecture**: Clear separation of `Domain`, `Application`, `Infrastructure`, and `Adapters`.
* **CQRS Pattern**:
* **Write Model**: Aggregates handling business logic and consistency.
* **Read Model**: Optimized JPQL/SQL queries for high-performance data fetching.


* **Security**:
* **OAuth2 Resource Server**: Stateless JWT validation.
* **Keycloak Integration**: Delegated identity management (IdP).
* **Authorization**: Tenant-level isolation.


* **Event-Driven**: Decoupled side effects using Spring Events (e.g., Welcome Emails).
* **Modern Stack**: Java 25 features (Records, Pattern Matching), Spring Boot 4.0.2, PostgreSQL 16.

## Tech Stack

* **Language**: Java 25
* **Framework**: Spring Boot 4.0.2
* **Build Tool**: Maven
* **Database**: PostgreSQL 16
* **Identity Provider**: Keycloak 26.0
* **Observability**: OpenTelemetry, Prometheus, Grafana, Tempo
* **Containerization**: Docker & Docker Compose

## Project Structure

The code follows the **Package by Component / Layer** structure:

```text
src/main/java/pl/jakubsiekiera/iam/
├── adapters/           # (Primary Adapters) REST Controllers, Web logic
├── application/        # (Application Layer) Service orchestration, DTOs
│   ├── service/        # Command Services (Write)
│   ├── query/          # Query Services (Read)
│   └── listener/       # Event Listeners (Side effects)
├── domain/             # (Domain Layer) Pure Java business logic
│   ├── model/          # Aggregates, Entities, Value Objects
│   ├── repository/     # Repository Interfaces
│   └── event/          # Domain Events
└── infrastructure/     # (Secondary Adapters) Persistence, Config, Security
    ├── persistence/    # JPA Entities, Spring Data Repositories
    └── config/         # Spring Security, App Config

```

## Getting Started

### Prerequisites

* Java 25
* Maven 3.9+
* Docker & Docker Compose

### 1. Start Infrastructure

Spin up PostgreSQL, Keycloak, and the Observability Stack using Docker Compose:

```bash
docker compose up -d

```

* **Postgres**: Port `5432`
* **Keycloak**: Port `8081`
* **Grafana**: Port `3000` (Visualization)
* **Prometheus**: Port `9090` (Metrics)
* **Tempo**: Port `3200` (Tracing)

### 2. Configure Keycloak

Since Keycloak starts empty in dev mode, you need to set it up:

1. Access [http://localhost:8081](https://www.google.com/search?q=http://localhost:8081) (Admin: `admin` / `admin`).
2. Create Realm: `saas-iam`.
3. Create Client: `iam-service` (Access Type: **Public** / Client Auth: **Off**).
4. Create User: `user1` (Set password to `password`, ensure **Temporary** is **OFF**).

### 3. Run the Application

```bash
mvn spring-boot:run

```

The app will start on `http://localhost:8080`.

## API Usage Guide

### 1. Register a Tenant (Public)

Creates a new tenant instance.

```bash
curl -i -X POST http://localhost:8080/api/v1/tenants \
  -H "Content-Type: application/json" \
  -d '{"name": "TechCorp", "email": "admin@techcorp.com"}'

```

**Response**: `201 Created` (Save the `id` from the response!)

### 2. Register a User Profile (Public)

Creates a user profile in the local database (simulates syncing from Keycloak).

```bash
curl -i -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"email": "employee@techcorp.com", "keycloakId": "kc-user-123"}'

```

### 3. Get Access Token

Authenticate with Keycloak to get a JWT.

```bash
curl -X POST http://localhost:8081/realms/saas-iam/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=iam-service" \
  -d "username=user1" \
  -d "password=password" \
  -d "grant_type=password"

```

**Response**: JSON with `"access_token"`.

### 4. Invite User to Tenant (Secured)

Links the user to the tenant with a specific role. Requires the **Token**.

```bash
# Replace TENANT_ID and TOKEN
curl -i -X POST http://localhost:8080/api/v1/tenants/TENANT_ID/users \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"email": "employee@techcorp.com", "role": "ADMIN"}'

```

### 5. View Tenant Details (CQRS Read Model)

Fetches tenant details and the efficient membership list.

```bash
curl -i -X GET http://localhost:8080/api/v1/tenants/TENANT_ID \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

```

**Response**:

```json
{
  "id": "...",
  "name": "TechCorp",
  "status": "ACTIVE",
  "members": [
    { "email": "employee@techcorp.com", "roles": "ADMIN" }
  ]
}

```

## Observability

The project includes a pre-configured observability stack to monitor metrics and distributed traces.

### Grafana Dashboards
Access Grafana at [http://localhost:3000](http://localhost:3000).
* **Credentials**: Anonymous admin access is enabled for development (no login required).
* **Dashboards**: A pre-provisioned **IAM Service Overview** dashboard is available, showing:
    *   Request rates and latencies.
    *   JVM metrics (Memory, GC, Threads).
    *   Keycloak metrics.

### Distributed Tracing
* **Tempo** is configured as the tracing backend.
* **OpenTelemetry Collector** aggregates traces from the application and Keycloak.
* You can view traces in Grafana using the **Explore** tab or by clicking on trace IDs in logs/dashboards.

## Architecture Decisions

| Concept | Implementation | Reasoning |
| --- | --- | --- |
| **Identity Source** | **Keycloak** | Don't reinvent the wheel. Keycloak handles passwords, 2FA, and sessions securely. |
| **Separation** | **Tenant vs User** | Users can belong to multiple tenants (Many-to-Many). Modeled as two separate aggregates. |
| **Persistence** | **PostgreSQL + JPA** | Reliable ACID transactions. Mapped manually from Domain to JPA entities to keep Domain pure. |
| **Reads** | **CQRS** | The `TenantQueryService` bypasses the Domain Model and queries DB tables directly for performance. |

## Future Roadmap

* [ ] **Audit Logging**: Persist domain events to a dedicated audit store.
* [ ] **Tenant Deletion**: Implement "Suspend" and "Soft Delete" logic.
* [ ] **Frontend**: React application with OIDC integration.

---

**Author**: Jakub Siekiera
**License**: MIT