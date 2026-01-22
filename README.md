# DTU Pay (Multi-Module)

## Commands

### Start services
```
docker compose up -d --build
```

### Unit + service tests
```
mvn -P service-tests verify
```

### E2E tests
```
SERVER_URL=http://localhost:8080 mvn -P e2e -pl e2e-tests -am verify
```

### Stop services
```
docker compose down
```
