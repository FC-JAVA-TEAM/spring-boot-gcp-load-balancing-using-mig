FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/spring-boot-gcp-load-balancing-using-mig-0.0.1-SNAPSHOT.jar app.jar

ENV ZONE=default-zone

EXPOSE 1212

ENTRYPOINT ["java", "-jar", "app.jar"]
