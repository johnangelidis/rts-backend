# RTS Backend

Spring Boot API for the RTS Application. It provides user authentication and a favorites watchlist.

## Requirements

- Java 17+
- Maven 3.6+
- PostgreSQL

The database must contain the `users` and `favorites` tables. The `favorites` table includes `ticker` and non-null numeric `opening_price` columns.

## Run

```bash
mvn spring-boot:run
```

The API runs at:

```text
http://localhost:8080/rts-backend
```

## Main endpoints

```text
POST   /api/v1/auth/signup
POST   /api/v1/auth/login
POST   /api/v1/favorites
GET    /api/v1/favorites/user/{userId}
DELETE /api/v1/favorites/{favoriteId}
```

Create a favorite with:

```json
{
  "userId": 1,
  "ticker": "AAPL",
  "openingPrice": 150.25
}
```

## Tests

```bash
mvn test
```