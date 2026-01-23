#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

compose() {
  docker compose -f "${ROOT_DIR}/docker-compose.yml" "$@"
}

cleanup() {
  compose down --remove-orphans
}
trap cleanup EXIT

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is required for health checks. Install it and re-run."
  exit 1
fi

echo "Cleaning old test reports..."
find "${ROOT_DIR}" -path '*/target/surefire-reports/*.xml' -delete || true
find "${ROOT_DIR}" -path '*/target/failsafe-reports/*.xml' -delete || true

echo "Building images..."
compose build

echo "Starting services..."
compose up -d

echo "Waiting for API Gateway at http://localhost:8080 ..."
for i in {1..60}; do
  if curl -fsS http://localhost:8080 >/dev/null 2>&1; then
    echo "API Gateway is up."
    break
  fi
  sleep 2
  if [ "$i" -eq 60 ]; then
    echo "API Gateway did not become ready in time."
    compose ps
    exit 1
  fi
done

echo "Running unit + service tests..."
mvn -f "${ROOT_DIR}/pom.xml" test

echo "Running end-to-end tests..."
SERVER_URL=http://localhost:8080 mvn -f "${ROOT_DIR}/pom.xml" -pl e2e-tests -am test

echo "All tests completed."
