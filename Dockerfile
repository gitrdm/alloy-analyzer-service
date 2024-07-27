FROM maven:3.9-eclipse-temurin-21-jammy AS build

WORKDIR /app

# download dependencies
COPY pom.xml .
COPY lib ./lib
RUN mvn validate dependency:go-offline -B


COPY src ./src
RUN mvn package -DskipTests
RUN ls /app/target

FROM maven:3.9-eclipse-temurin-21-jammy
#FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/target/AlloyAnalyzerService.jar /app/AlloyAnalyzerService.jar

CMD ["java", "-jar", "/app/AlloyAnalyzerService.jar"]
