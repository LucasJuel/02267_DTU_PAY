.PHONY: up down build test-service test-e2e test-all

up:
	docker compose up -d --build

down:
	docker compose down

build:
	mvn -DskipTests package

test-service:
	mvn -P service-tests verify

test-e2e:
	SERVER_URL=http://localhost:8080 mvn -P e2e -pl e2e-tests -am verify

test-all: test-service test-e2e
