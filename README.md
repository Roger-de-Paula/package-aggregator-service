# Package Aggregation Service

[![Tests](https://github.com/roger/package-aggregator-service/actions/workflows/tests.yml/badge.svg)](https://github.com/roger/package-aggregator-service/actions/workflows/tests.yml)

A production-oriented take-home assessment implementing a **Package Aggregation Service** that depends on unreliable third-party APIs. Built with Clean Architecture and SOLID principles.

---

## Architecture

### Layers (Clean Architecture)

- **API / Controller**: HTTP entrypoints. Validate input, delegate to services, return DTOs. No business logic.
- **Service**: All business logic (snapshotting, total calculation, currency conversion on read, soft delete).
- **Repository**: JPA data access only. `PackageJpaRepository`, `PackageProductJpaRepository`.
- **Client**: Outbound integration. `ProductClient` (WebClient, basic auth, timeout, retries), `ExchangeRateClient` (cached).
- **Domain**: Entities only. `PackageEntity`, `PackageProductEntity`.

### Transaction boundary

Package creation **does not** call the external API inside a DB transaction:

1. **Fetch and validate** all products via `ProductClient.getProductsByIds(ids)` (outside any transaction).
2. **Then** a single short `@Transactional` method persists the package and product snapshots.

This avoids holding a DB connection while the external API is called (connection pool exhaustion and timeouts if the API hangs).

### Why Snapshotting?

We **do not** store only `productIds`. At package creation we:

1. Fetch each product from the external Product API (batch/parallel; see above).
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

### Caching and batch strategy

- **Spring Cache + Caffeine**
- **Products**: TTL 30 minutes, key by product id. `ProductClient.getProductById(id)` is cached; **`getProductsByIds(ids)`** fetches all requested ids **in parallel** (and uses the same cache), so package creation with 8 products does not block for 8× round-trip time.
- **Exchange rates**: `@Cacheable("exchangeRates")` (Caffeine, 1 hour TTL) on `ExchangeRateClient.getRateUsdTo(currency)`. Rates change ~once per day, so we avoid repeated calls to the provider. List and detail reuse the same rate per request; Frankfurter is not called on every package row.

### Resilience Strategy

- **ProductClient**
  - WebClient with **3 second** connect/read/write timeout.
  - **2 retries** with 500 ms delay on 5xx only.
  - Throws `ExternalServiceUnavailableException` on failure (mapped to **503** in `GlobalExceptionHandler`).
- **ExchangeRateClient**
  - 1-hour cache to avoid repeated calls and to tolerate short outages.
  - Throws same exception on failure.
- **Controller advice** (`GlobalExceptionHandler`) maps:
  - `PackageNotFoundException` → **404**
  - `InvalidProductException` (invalid or missing product id/price) → **400 Bad Request**
  - Validation / `IllegalArgumentException` → **400**
  - `ExternalServiceUnavailableException` (product or exchange-rate API down/timeout/rate limit) → **503 Service Unavailable**  
  **500** means our system is broken; **503** means a dependency is temporarily unavailable. Aggregators must distinguish these so callers can retry or degrade gracefully.

### Tradeoffs

- **Snapshot at create time**: More storage and a heavier create flow, but correct and resilient reads. Acceptable for an aggregation service.
- **Blocking WebClient**: We use `.block()` in clients for simplicity. A full reactive stack would avoid blocking but increase complexity.
- **Single cache manager for products**: One TTL for all product entries. Fine for this scope; could be per-key TTL if needed.
- **Soft delete**: Packages are soft-deleted (flag on entity). **All read operations** use `findByIdAndDeletedFalse` / `findAllByDeletedFalse`; deleted packages never appear in list or get-by-id. They remain in the DB for audit and referential integrity.

### Design decision: package composition immutable after creation

**Package composition (product list) is immutable after creation** to preserve historical price integrity and auditability. The update endpoint allows changing only **name** and **description**. This is a common pattern in commerce: once a package is created, its snapshot of products and prices is fixed so that past orders and reports remain consistent. If product composition had to change, the design would support a new version or a new package instead of mutating the existing one.

---

## Tech Stack

**Backend**

- Java 11, Spring Boot 2.7
- Spring Web, Spring Data JPA (Hibernate), H2 (in-memory)
- Spring Cache (Caffeine), WebClient (no RestTemplate), Lombok  
- Build: Maven

**Frontend**

- React (Vite), **Axios** for all HTTP calls (single configured instance, typed responses), React Router
- Functional components, minimal UI

**CI**

- GitHub Actions (`.github/workflows/tests.yml`): backend tests (`./mvnw test`) and frontend build (`npm ci && npm run build`) on push and pull_request to `main`.

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
- **Swagger UI (API docs):** http://localhost:8080/swagger-ui.html  
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs  

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

## API Documentation (Swagger / OpenAPI 3)

The backend exposes **OpenAPI 3** documentation:

- **Swagger UI:** http://localhost:8080/swagger-ui.html — browse all endpoints, see request/response schemas, and try requests.
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs — machine-readable spec.

Setup follows common practices:

- **springdoc-openapi-ui** (no Springfox); one `OpenAPI` bean with title, description, version, and server.
- **Tags** group endpoints (Packages, Products, Currencies).
- **@Operation** on each method with summary and description; **@ApiResponses** for success and error codes (400, 404, 503).
- **@Parameter** for path/query args; **@Schema** on key DTOs for clear request/response models.

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET    | `/currencies` | **Internal.** Supported currencies (from Frankfurter). Query: `search` (optional, filters by code or name). Cached. Returns `[{ code, name }]`. |
| GET    | `/products` | **Internal.** Product catalog for the frontend (e.g. to build a package by selection). Returns list of `{ id, name, price, currency }`. Cached. |
| POST   | `/packages` | Create package (body: name, description, productIds). **Validated**: `name` @NotBlank, `productIds` @NotEmpty (at least one product). Snapshots products, stores in USD. |
| GET    | `/packages` | List packages (paginated). Query: `page`, `size`, `currency` (default USD). |
| GET    | `/packages/{id}` | Get one package. Query: `currency` (optional). |
| PUT    | `/packages/{id}` | Update name and description only. **Product composition is immutable after creation** to preserve price history integrity. |
| DELETE | `/packages/{id}` | Soft delete. **Idempotent**: second delete returns 204. |

---

## External Services

- **Product API**: `https://product-service.herokuapp.com/api/v1/products` and `/products/{id}` (Basic auth: user / pass).
- **Exchange rates**: `https://api.frankfurter.app/latest?from=USD&to={currency}`.

The service remains usable when these are slow or temporarily unavailable thanks to caching, timeouts, retries, and snapshotting.
