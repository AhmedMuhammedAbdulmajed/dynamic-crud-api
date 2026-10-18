# Dynamic CRUD API

Spring Boot REST API that exposes **generic CRUD** for any JPA entity you register. Instead of writing a controller/service/repository per model, you:

1. Create a JPA `@Entity` class  
2. Register its URL name in `EntityRegistry`  
3. Use `/api/{entity}` for list, get, create, update, and delete  

Version: `1.0.0` · Package: `com.example.dynamiccrud` · Port: `8080`

## Features

- Single dynamic controller for all registered entities
- Entity name → class mapping via `EntityRegistry`
- Generic persistence through `EntityManager` (`GenericRepository`)
- JSON request bodies mapped with Jackson (`Map` → entity)
- Partial updates (only non-null fields are copied on `PUT`)
- Centralized error responses (`GlobalExceptionHandler`)
- H2 in-memory DB with web console
- OpenAPI / Swagger UI (`springdoc`)
- Sample entities: `Product`, `Customer`

## How it works

```text
HTTP /api/{entity}
        │
        ▼
DynamicCrudController
        │
        ▼
GenericCrudService  ──resolve──►  EntityRegistry  (name → Class)
        │
        ▼
GenericRepository  ──JPA──►  H2 (jdbc:h2:mem:dynamiccrud)
```

1. Request hits `/api/{entity}` (e.g. `products`).
2. `EntityRegistry` resolves the path segment to a Java class.
3. `GenericCrudService` converts JSON to that class and runs CRUD.
4. Unknown entity names return `404` with the list of registered names.

## Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Web | spring-boot-starter-web |
| Persistence | Spring Data JPA + Hibernate |
| Database | H2 (in-memory) |
| Validation | spring-boot-starter-validation |
| API docs | springdoc-openapi 2.6.0 |
| Build | Maven (wrapper included) |

## Prerequisites

- JDK 17+
- Maven 3.9+ **or** use `./mvnw` / `mvnw.cmd` (no global Maven required)

## Quick start

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Or with a local Maven install:

```bash
mvn spring-boot:run
```

| Resource | URL |
|----------|-----|
| API base | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 console | http://localhost:8080/h2-console |
| Simple UI (static) | http://localhost:8080/ |

### H2 console login

| Setting | Value |
|---------|--------|
| JDBC URL | `jdbc:h2:mem:dynamiccrud` |
| User | `sa` |
| Password | *(empty)* |

Schema is created/updated automatically (`spring.jpa.hibernate.ddl-auto=update`). Data is lost when the process stops (in-memory).

## Project structure

```text
dynamic-crud-api/
├── pom.xml
├── mvnw / mvnw.cmd
├── README.md                 # this file
├── BRANCHING.md              # Git branch & PR rules
└── src/main/
    ├── java/com/example/dynamiccrud/
    │   ├── DynamicCrudApplication.java   # entry point
    │   ├── config/
    │   │   └── EntityRegistry.java       # name → entity class map
    │   ├── controller/
    │   │   └── DynamicCrudController.java
    │   ├── entity/
    │   │   ├── Product.java
    │   │   └── Customer.java
    │   ├── exception/
    │   │   └── GlobalExceptionHandler.java
    │   ├── repository/
    │   │   └── GenericRepository.java    # EntityManager CRUD
    │   └── service/
    │       └── GenericCrudService.java
    └── resources/
        ├── application.yml
        └── static/index.html
```

### Main components

| Class | Role |
|-------|------|
| `EntityRegistry` | Registers URL segments (`products`, `customers`, …) to entity classes at startup |
| `DynamicCrudController` | REST endpoints under `/api` |
| `GenericCrudService` | Resolve entity, map JSON, create/update/delete logic |
| `GenericRepository` | Low-level JPA `find` / `persist` / `merge` / `remove` by class |
| `GlobalExceptionHandler` | Consistent JSON errors for 404, validation, and unexpected failures |

## Registered entities

| Path | Class | Table | Fields |
|------|-------|-------|--------|
| `products` | `Product` | `products` | `id`, `name` (required), `description`, `price` (≥ 0, required), `quantity` (≥ 0) |
| `customers` | `Customer` | `customers` | `id`, `name` (required), `email` (required, unique), `phone`, `address` |

List registered names:

```bash
curl http://localhost:8080/api/entities
```

Example response: `["products","customers"]`

## API reference

All entity paths are case-insensitive in the registry (`Products` → `products`).

| Method | Endpoint | Status | Description |
|--------|----------|--------|-------------|
| `GET` | `/api/entities` | `200` | List registered entity names |
| `GET` | `/api/{entity}` | `200` | List all rows |
| `GET` | `/api/{entity}/{id}` | `200` / `404` | Get one by id |
| `POST` | `/api/{entity}` | `201` | Create (body: JSON object; `id` is ignored/cleared) |
| `PUT` | `/api/{entity}/{id}` | `200` / `404` | Update; only non-null fields overwrite existing values |
| `DELETE` | `/api/{entity}/{id}` | `204` / `404` | Delete by id |

### Error body

```json
{
  "timestamp": "2026-10-18T13:00:00Z",
  "status": 404,
  "error": "products with id 99 not found"
}
```

Typical cases:

- Unknown `{entity}` → `404` with registered names listed  
- Missing id → `404`  
- Bad JSON / illegal argument → `400`  
- Unexpected failure → `500`

## Examples

### Products

**Create**

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Laptop\",\"description\":\"15-inch\",\"price\":999.99,\"quantity\":10}"
```

**List / get / update / delete**

```bash
curl http://localhost:8080/api/products
curl http://localhost:8080/api/products/1

curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d "{\"price\":899.99,\"quantity\":5}"

curl -X DELETE http://localhost:8080/api/products/1
```

### Customers

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Jane Doe\",\"email\":\"jane@example.com\",\"phone\":\"555-0100\",\"address\":\"Cairo\"}"

curl http://localhost:8080/api/customers
```

## Add a new entity

1. Create a JPA entity under `entity/` (must have a `Long id` field for create/update helpers):

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // fields + getters/setters
}
```

2. Register it in `EntityRegistry.init()`:

```java
register("orders", Order.class);
```

3. Restart the app — endpoints are available at `/api/orders` with no new controller code.

## Configuration

Key settings in `src/main/resources/application.yml`:

| Key | Value | Notes |
|-----|-------|--------|
| `server.port` | `8080` | HTTP port |
| `spring.datasource.url` | `jdbc:h2:mem:dynamiccrud;...` | In-memory DB |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto schema |
| `spring.jpa.show-sql` | `true` | SQL logged |
| `spring.h2.console.enabled` | `true` | Browser console |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | API docs UI |
| Logging | `com.example.dynamiccrud: DEBUG` | App debug logs |

## Build & test

```bash
mvn test
mvn package
```

Runnable JAR: `target/dynamic-crud-api-1.0.0.jar`

```bash
java -jar target/dynamic-crud-api-1.0.0.jar
```

## Branching

Long-lived branches: `develop` (dev) → `qa` (QA) → `main` (production).

Anyone may create topic branches and open PRs; only the owner merges into `develop`, `qa`, and `main`.

Full rules: [BRANCHING.md](BRANCHING.md).
