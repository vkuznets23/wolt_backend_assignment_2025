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
