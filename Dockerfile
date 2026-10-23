FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system spring \
    && useradd --system --gid spring --home-dir /app --shell /usr/sbin/nologin spring

COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

USER spring:spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
