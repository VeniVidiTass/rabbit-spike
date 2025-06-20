# Stage 1: build
FROM maven:3.8.7-eclipse-temurin-17 AS build
LABEL authors="GestMed"
WORKDIR /app

# Copia Maven wrapper e pom
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Copia il sorgente e compila
COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: runtime
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
