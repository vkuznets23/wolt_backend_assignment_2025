# DOPC Backend

Delivery Order Price Calculator — Spring Boot service for the Wolt backend home assignment. It exposes a single public API that loads venue **static** and **dynamic** data from the **Home Assignment API** [static](https://consumer-api.development.dev.woltapi.com/home-assignment-api/v1/venues/home-assignment-venue-helsinki/static) and [dynamic](https://consumer-api.development.dev.woltapi.com/home-assignment-api/v1/venues/home-assignment-venue-helsinki/dynamic), then computes delivery distance, fees, small-order surcharge, and total price (amounts in the smallest currency unit).

## Requirements

- **JDK 21**

Check your Java version:

```bash
java -version
```

## Run the application

From the repository root:

```bash
cd dopc
./gradlew bootRun
```

The server listens on **http://localhost:8080** by default (Spring Boot default port).

**Note:** Running the delivery price endpoint requires **outbound internet access** to the Home Assignment API.

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
- Upstream issues (e.g. unknown venue, timeout, empty body) → **404**, **400**, **502**, or **504** as mapped by the HTTP client layer.

## Architecture context

- `DeliveryOrderPriceController` — handles `GET /api/v1/delivery-order-price`, binds query parameters, delegates validation and pricing flow
- `ValidationService` — input checks before business calculations
- `GlobalExceptionHandler` — unified JSON error responses for validation/binding/unexpected errors

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

Module entrypoint: `com.example.dopc.DopcApplication`.

## Tech stack

- Kotlin, Spring Boot 3.x, Spring Web
- Gradle (Kotlin DSL), JUnit 5 for tests
