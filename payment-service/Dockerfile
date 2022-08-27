FROM openjdk:8-jdk-alpine
EXPOSE 8083
COPY build/libs/payment-service-1.0-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "payment-service-1.0-SNAPSHOT.jar"]