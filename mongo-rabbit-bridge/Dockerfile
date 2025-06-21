# Dockerfile for mongo-rabbit-bridge with shared lib support
FROM maven:3.8.7-eclipse-temurin-17 AS build

# First build and install the shared module
WORKDIR /app/shared
COPY shared/pom.xml ./
COPY shared/src ./src
RUN mvn clean install -DskipTests

# Then build the mongo-rabbit-bridge module
WORKDIR /app/mongo-rabbit-bridge
COPY mongo-rabbit-bridge/pom.xml ./
COPY mongo-rabbit-bridge/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /app/mongo-rabbit-bridge/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]