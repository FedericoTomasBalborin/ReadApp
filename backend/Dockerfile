# generamos fat jar de Politics Spring boot app con gradle
FROM gradle:8.8-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
ADD . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle bootJar --no-daemon

# ejecutamos el JAR resultante
FROM eclipse-temurin:21.0.2_13-jre-jammy
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/booklibre-back-grupo1-1.0-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
