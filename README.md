# Dynamic CRUD API

Spring Boot API that exposes generic CRUD endpoints for registered JPA entities. Add an entity class, register it in `EntityRegistry`, and it becomes available under `/api/{entity}`.

## Stack

- Java 17
- Spring Boot 3.3
- Spring Data JPA
- H2 in-memory database

## Project structure

```
dynamic-crud-api/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/example/dynamiccrud/
    │   ├── DynamicCrudApplication.java
    │   ├── config/EntityRegistry.java
    │   ├── controller/DynamicCrudController.java
    │   ├── entity/Product.java
    │   ├── entity/Customer.java
    │   ├── exception/GlobalExceptionHandler.java
    │   ├── repository/GenericRepository.java
    │   └── service/GenericCrudService.java
    └── resources/application.yml
```

## Run

```bash
mvn spring-boot:run
```

App starts at `http://localhost:8080`. H2 console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:dynamiccrud`).

## Registered entities

| Path segment | Entity class |
|--------------|--------------|
| `products`   | `Product`    |
| `customers`  | `Customer`   |

List them: `GET /api/entities`

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/{entity}` | List all |
| GET | `/api/{entity}/{id}` | Get by id |
| POST | `/api/{entity}` | Create |
| PUT | `/api/{entity}/{id}` | Update |
| DELETE | `/api/{entity}/{id}` | Delete |

### Examples

**Create product**

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Laptop\",\"description\":\"15-inch\",\"price\":999.99,\"quantity\":10}"
```

**Create customer**

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Jane Doe\",\"email\":\"jane@example.com\",\"phone\":\"555-0100\"}"
```

**List products**

```bash
curl http://localhost:8080/api/products
```

## Add a new entity

1. Create a JPA `@Entity` under `entity/`.
2. Register it in `EntityRegistry.init()`:

```java
register("orders", Order.class);
```

3. Restart the app — CRUD is available at `/api/orders`.
