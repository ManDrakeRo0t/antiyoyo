#
# Build stage
#
FROM maven:3.9-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -Pprod -DskipTests

#
# Package stage
#
FROM openjdk:21-jdk-slim
COPY --from=build /target/antiyoyo-0.0.1-SNAPSHOT.jar app.jar
# ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]