.PHONY: up down build test-service test-e2e test-all

up:
	docker compose up -d --build

down:
	docker compose down

build:
	mvn -DskipTests package

test-service:
	mvn test

test-e2e:
	SERVER_URL=http://localhost:8080 mvn -pl e2e-tests -am test

test-all: test-service test-e2e
