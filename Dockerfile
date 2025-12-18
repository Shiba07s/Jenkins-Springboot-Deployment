#FROM openjdk:17-jdk-slim
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app
COPY target/product-service.jar product-service.jar
EXPOSE 2020
ENTRYPOINT ["java","-jar","product-service.jar"]