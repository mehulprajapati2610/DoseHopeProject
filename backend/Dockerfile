FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY backend/pom.xml ./
COPY backend/src ./src
COPY frontend ../frontend
RUN mvn -DskipTests package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=default
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]