FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew clean bootJar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/defaultsecurity-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAM=512m","-XX:MaxRAMPercentage=60","-XX:InitialRAMPercentage=25","-XX:MaxMetaspaceSize=160m","-XX:+UseG1GC","-jar","app.jar", "--spring.profiles.active=devdeploy"]