# DOPC Backend

## Requirements

- JDK 21

Check Java version:

```bash
java -version
```

## Run the server

```bash
cd dopc
./gradlew bootRun
```

Server starts on: http://localhost:8080

## Health check endpoints

`GET` / -> DOPC API is running
`GET` /health -> OK
`GET` /venue/home-assignment-venue-stockholm/static -> show static data fetched from external API
`GET` /venue/home-assignment-venue-stockholm/dynamic -> show dynamic data fetched from external API
