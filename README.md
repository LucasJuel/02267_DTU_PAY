# DTU Pay (Multi-Module)

## Commands

### Start services
```
docker compose up -d --build
```

### Unit + service tests
```
mvn test
```

### E2E tests
```
SERVER_URL=http://localhost:8080 mvn -pl e2e-tests -am test
```

### Stop services
```
docker compose down
```
