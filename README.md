# Package Aggregation Service

A production-oriented take-home assessment implementing a **Package Aggregation Service** that depends on unreliable third-party APIs. Built with Clean Architecture and SOLID principles.

---

## Architecture

### Layers (Clean Architecture)

- **API / Controller**: HTTP entrypoints. Validate input, delegate to services, return DTOs. No business logic.
- **Service**: All business logic (snapshotting, total calculation, currency conversion on read, soft delete).
- **Repository**: JPA data access only. `PackageJpaRepository`, `PackageProductJpaRepository`.
- **Client**: Outbound integration. `ProductClient` (WebClient, basic auth, timeout, retries), `ExchangeRateClient` (cached).
- **Domain**: Entities only. `PackageEntity`, `PackageProductEntity`.

### Why Snapshotting?

We **do not** store only `productIds`. At package creation we:

1. Fetch each product from the external Product API.
2. Persist **snapshots**: `externalProductId`, `productName`, `productPriceUsd` in `PackageProductEntity`.
3. Store `totalPriceUsd` on `PackageEntity`.

**Reasons:**

- **Historical correctness**: External prices change. A package created yesterday must always show the same total and line items.
- **Determinism**: No dependency on the external API when reading packages.
- **Resilience**: If the product service is down, we can still list and display existing packages.

### Internal Currency (USD Only)

- All monetary values are stored in **USD** in the database.
- Conversion to EUR, GBP, etc. happens **only at response time** via `ExchangeRateClient`.
- We never persist converted amounts.

### Caching Strategy

- **Spring Cache + Caffeine**
- **Products**: TTL 30 minutes. Key: product id. Reduces calls to the product service when creating multiple packages or retrying.
- **Exchange rates**: TTL 1 hour, separate cache manager. Key: target currency. Keeps list/detail responses fast and avoids hitting Frankfurter on every request.

### Resilience Strategy

- **ProductClient**
  - WebClient with **3 second** connect/read/write timeout.
  - **2 retries** with 500 ms delay on 5xx only.
  - Throws `ExternalServiceUnavailableException` on failure (mapped to **503** in `GlobalExceptionHandler`).
- **ExchangeRateClient**
  - 1-hour cache to avoid repeated calls and to tolerate short outages.
  - Throws same exception on failure.
- **Controller advice** maps:
  - `PackageNotFoundException` → **404**
  - Validation / `IllegalArgumentException` → **400**
  - `ExternalServiceUnavailableException` → **503**

### Tradeoffs

- **Snapshot at create time**: More storage and a heavier create flow, but correct and resilient reads. Acceptable for an aggregation service.
- **Blocking WebClient**: We use `.block()` in clients for simplicity. A full reactive stack would avoid blocking but increase complexity.
- **Single cache manager for products**: One TTL for all product entries. Fine for this scope; could be per-key TTL if needed.
- **Soft delete**: Deleted packages are hidden from queries but kept in DB for audit; no hard delete to avoid breaking referential integrity and history.

---

## Tech Stack

**Backend**

- Java 11, Spring Boot 2.7
- Spring Web, Spring Data JPA (Hibernate), H2 (in-memory)
- Spring Cache (Caffeine), Actuator, WebClient (no RestTemplate), Lombok  
- Build: Maven

**Frontend**

- React (Vite), Axios, React Router
- Functional components, minimal UI

---

## How to Run

### Option 1: Docker (easiest)

Running the app in Docker is the simplest way—you don’t need to run the frontend and backend separately or install Java/Node locally.

**Prerequisites:** [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/install/).

From the project root:

```bash
docker compose up --build
```

- **Frontend:** http://localhost:5173  
- **Backend API:** http://localhost:8080  
- **Health:** http://localhost:8080/actuator/health  

Stop with `Ctrl+C`, or run in the background with `docker compose up -d --build`.

---

### Option 2: Run backend and frontend separately

#### Prerequisites

- **Java 11+**
- **Node.js 18+** (for frontend)
- **Maven** (or use wrapper `mvnw.cmd`)

#### Backend

```bash
# From project root
./mvnw spring-boot:run
# Or
mvn spring-boot:run
```

- Server: **http://localhost:8080**
- Health: **http://localhost:8080/actuator/health**
- API base: **http://localhost:8080/packages**

#### Frontend

```bash
cd frontend
npm install
npm run dev
```

- App: **http://localhost:5173**
- Ensure the backend is running so API calls to `http://localhost:8080` succeed (CORS is allowed for `http://localhost:5173`).

#### Optional: Run tests

```bash
./mvnw test
```

Tests mock `ProductClient` and `ExchangeRateClient` so they do not call real APIs.

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET    | `/products` | **Internal.** Product catalog for the frontend (e.g. to build a package by selection). Returns list of `{ id, name, usdPrice }`. Cached. |
| POST   | `/packages` | Create package (body: name, description, productIds). Snapshots products, stores in USD. |
| GET    | `/packages` | List packages (paginated). Query: `page`, `size`, `currency` (default USD). |
| GET    | `/packages/{id}` | Get one package. Query: `currency` (optional). |
| PUT    | `/packages/{id}` | Update name and description only (products immutable). |
| DELETE | `/packages/{id}` | Soft delete. |

---

## External Services

- **Product API**: `https://product-service.herokuapp.com/api/v1/products` and `/products/{id}` (Basic auth: user / pass).
- **Exchange rates**: `https://api.frankfurter.app/latest?from=USD&to={currency}`.

The service remains usable when these are slow or temporarily unavailable thanks to caching, timeouts, retries, and snapshotting.
