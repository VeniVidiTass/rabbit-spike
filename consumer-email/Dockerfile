# Dockerfile per Consumer Email con supporto shared lib
FROM maven:3.8.7-eclipse-temurin-17 AS build

# Prima costruisci e installa il modulo shared
WORKDIR /app/shared
COPY shared/pom.xml ./
COPY shared/src ./src
RUN mvn clean install -DskipTests

# Poi costruisci il consumer-email
WORKDIR /app/consumer-email
COPY consumer-email/pom.xml ./
COPY consumer-email/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /app/consumer-email/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
