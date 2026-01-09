FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM maven:3.9-eclipse-temurin-21
WORKDIR /app
COPY --from=build /app .

ENTRYPOINT ["tail", "-f", "/dev/null"]