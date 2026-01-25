FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew clean bootJar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/defaultsecurity-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-XX:+UseG1GC","-XX:MaxRAMPercentage=42","-XX:InitialRAMPercentage=20","-XX:MaxMetaspaceSize=140m","-XX:CompressedClassSpaceSize=48m","-XX:+ExitOnOutOfMemoryError","-jar","app.jar"]