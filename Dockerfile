FROM eclipse-temurin:17-jdk-jammy

WORKDIR ./bot

COPY slimebot.jar ./slimebot.jar
ADD ./run/* ./


ENTRYPOINT ["java", "-jar", "SlimeBot.jar"]