# RTS Backend

Spring Boot API for the RTS Application. It provides user authentication and a favorites watchlist.

## Requirements

- Java 17+
- Maven 3.6+
- PostgreSQL

Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SCHEMA`, `SERVER_PORT`, and `FINNHUB_API_KEY` in the environment before
starting the application.

The database must contain the `users` and `favorites` tables. The `favorites` table includes `ticker` and non-null
numeric `opening_price` columns.

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
GET    /api/v1/market/quote?symbol=AAPL
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
