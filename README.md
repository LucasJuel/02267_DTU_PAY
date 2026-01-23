# DTU Pay – Installation Guide

# Huba jenkins
password and for huba can be found in file: "huber_password_for_jenkins.txt"

## Repository URLs
<https://github.com/LucasJuel/02267_DTU_PAY>

## System Requirements
The following tools are required.

- **Java Development Kit (JDK) 21** (required by the project’s `maven.compiler.release=21`).
  - Download: <https://adoptium.net/temurin/releases/?version=21>
- **Apache Maven** (any Maven 3.x that supports JDK 21).
  - Download: <https://maven.apache.org/download.cgi>
- **Docker Engine** (with Docker Compose).
  - Download: <https://docs.docker.com/engine/install/>


## Build and Run the System
From the repository root:
```
docker compose up -d --build
```
This builds the Docker images and starts all services (API Gateway, microservices, and RabbitMQ).

To stop everything:
```
docker compose down
```

## Run Tests
### Unit + Service Tests
```
mvn test
```

### End-to-End Tests (against the running gateway on localhost)
```
SERVER_URL=http://localhost:8080 mvn -pl e2e-tests -am test
```

## One-Command Build + Deploy + Test
A Unix shell script is provided at the repository root and uses LF (Unix) line endings. It is executable and runs the full system and all tests without Jenkins:
```
./run-all-tests.sh
```

The script performs the following steps:
1) Stops any previous containers
2) Builds all Docker images
3) Starts the system
4) Runs `mvn test`
5) Runs E2E tests against the local API Gateway
6) Shuts everything down on exit

## Notes
- This guide assumes a Linux environment with Docker installed and the current user able to run Docker commands.
- No Jenkins setup is required to install or test the system.

## API Gateway Base URL
Local base URL: `http://localhost:8080`

## API Endpoints (API Gateway)
- `GET /hello`
- `POST /customer`
- `DELETE /customer/{customerId}`
- `POST /customer/{id}/report`
- `POST /merchant`
- `DELETE /merchant/{merchantId}`
- `POST /merchant/{id}/report`
- `POST /payment`
- `POST /manager/report`
