# DOPC Backend

Delivery Order Price Calculator — Spring Boot service for the Wolt backend home assignment. It exposes a single public API that loads venue **static** and **dynamic** data from the **Home Assignment API** [static](https://consumer-api.development.dev.woltapi.com/home-assignment-api/v1/venues/home-assignment-venue-helsinki/static) and [dynamic](https://consumer-api.development.dev.woltapi.com/home-assignment-api/v1/venues/home-assignment-venue-helsinki/dynamic), then computes delivery distance, fees, small-order surcharge, and total price (amounts in the smallest currency unit).

## Requirements

- **JDK 21**
- **Docker** (for local Redis cache)

Check your Java version:

```bash
java -version
```

## Run the application

From the repository root:

```bash
cd dopc
docker compose up -d
./gradlew bootRun
```

The server listens on **http://localhost:8080** by default (Spring Boot default port).

**Note:** Running the delivery price endpoint requires **outbound internet access** to the Home Assignment API.

Stop Redis when needed:

```bash
cd dopc
docker compose down
```

## Main API

### `GET /api/v1/delivery-order-price`

All query parameters are **required** and use **snake_case** (as in the assignment).

| Parameter    | Type    | Description                              |
| ------------ | ------- | ---------------------------------------- |
| `venue_slug` | string  | Venue identifier (slug)                  |
| `cart_value` | integer | Cart total in the smallest currency unit |
| `user_lat`   | number  | User latitude                            |
| `user_lon`   | number  | User longitude                           |

**Example**

```bash
curl "http://localhost:8080/api/v1/delivery-order-price?venue_slug=home-assignment-venue-helsinki&cart_value=1000&user_lat=60.17094&user_lon=24.93087"
```

**Example response** (JSON, snake_case)

```json
{
  "total_price": 1190,
  "small_order_surcharge": 0,
  "cart_value": 1000,
  "delivery": {
    "fee": 190,
    "distance": 177
  }
}
```

**Errors**
Errors are normalized by global exception handling into consistent error payload

- Invalid input (e.g. coordinates out of range, negative cart) → **400 Bad Request** with a short message.
- Delivery not possible for the computed distance → **400 Bad Request**.
- Upstream issues (e.g. unknown venue, timeout, empty body) → **404**, **400**, **502**, **504**, or **429** as mapped by the HTTP client layer.

## Redis cache

The service caches upstream venue payloads by `venue_slug`:

- Cache `static` (`/venues/{slug}/static`) → TTL **120s**
- Cache `dynamic` (`/venues/{slug}/dynamic`) → TTL **30s**

Implementation details:

- Caching is enabled via `@EnableCaching` in `DopcApplication`.
- `VenueDataCacheService` uses `@Cacheable` for `static` and `dynamic`.
- `RedisCacheConfig` configures:
  - Redis key serializer (`StringRedisSerializer`)
  - Typed value serializers:
    - `static` -> `StaticResponse`
    - `dynamic` -> `DynamicResponse`
  - Per-cache TTL settings

Useful Redis checks:

```bash
docker exec -it dopc-redis redis-cli KEYS "*"
docker exec -it dopc-redis redis-cli GET "static::home-assignment-venue-helsinki"
docker exec -it dopc-redis redis-cli TTL "static::home-assignment-venue-helsinki"
```

Flush cache during debugging:

```bash
docker exec -it dopc-redis redis-cli FLUSHALL
```

## Validation and retries

- Request parameters are validated declaratively in controller via `@Validated` + Bean Validation annotations (`@NotBlank`, `@Min`, `@DecimalMin`, `@DecimalMax`).
- Upstream HTTP calls in `HomeAssignmentClient` use `@Retryable` for transient failures:
  - `429 Too Many Requests`
  - `5xx` errors
  - network timeouts (`ResourceAccessException`)
- Retry policy: **max 3 attempts**, exponential backoff.
- `@Recover` methods map exhausted retries to API statuses:
  - `429` -> 429
  - `5xx` -> 502
  - timeout -> 504

## Architecture context

- `DeliveryOrderPriceController` — handles `GET /api/v1/delivery-order-price`, binds query parameters, validates inputs
- `DeliveryOrderPriceService` — computes distance, surcharge, delivery fee, total price
- `VenueDataCacheService` — cached access to upstream static/dynamic venue data
- `HomeAssignmentClient` — upstream API client with error mapping + retry/recover
- `GlobalExceptionHandler` — unified JSON error responses for validation, retry, and unexpected errors

## Other endpoints

| Method | Path      | Response                          |
| ------ | --------- | --------------------------------- |
| `GET`  | `/`       | Plain text: `DOPC API is running` |
| `GET`  | `/health` | Plain text: `OK`                  |

## Tests

```bash
cd dopc
./gradlew test
```

Client test focus:

- Mapping upstream errors to API statuses
- No-retry behavior for non-retriable cases (`404`, generic `4xx`, invalid payload)
- Retry x3 behavior for retriable cases (`429`, `5xx`, timeouts)

Module entrypoint: `com.example.dopc.DopcApplication`.

## Tech stack

- Kotlin, Spring Boot 3.x, Spring Web
- Spring Validation, Spring Retry, Spring Cache, Spring Data Redis
- Gradle (Kotlin DSL), JUnit 5 for tests
- Redis (Docker Compose for local dev)
