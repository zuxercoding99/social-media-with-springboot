FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew clean bootJar

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/defaultsecurity-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Xms128m","-Xmx384m","-jar","app.jar"]