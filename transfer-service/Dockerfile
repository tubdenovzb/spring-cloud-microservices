FROM openjdk:8-jdk-alpine
EXPOSE 8085
COPY build/libs/transfer-service-1.0-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "transfer-service-1.0-SNAPSHOT.jar"]