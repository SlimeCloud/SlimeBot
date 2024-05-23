FROM eclipse-temurin:17-jdk-jammy

WORKDIR ./bot

COPY ./run/* ./
COPY build/libs/*-all.jar ./SlimeBot.jar


ENTRYPOINT ["java", "-jar", "SlimeBot.jar"]