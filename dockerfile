# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY ingestion/pom.xml .
COPY ingestion/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/ingestion-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]