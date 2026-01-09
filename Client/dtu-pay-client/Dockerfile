FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY . .

RUN mvn clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar client-app.jar

EXPOSE 8181

ENTRYPOINT ["java", "-jar", "client-app.jar"]