# Dockerfile per Consumer Email con supporto shared lib
FROM maven:3.8.7-eclipse-temurin-17 AS build

# Prima costruisci e installa il modulo shared
WORKDIR /app/shared
COPY shared/pom.xml ./
COPY shared/src ./src
RUN mvn clean install -DskipTests

# Poi costruisci il consumer-sms
WORKDIR /app/consumer-sms
COPY consumer-sms/pom.xml ./
COPY consumer-sms/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /app/consumer-sms/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
