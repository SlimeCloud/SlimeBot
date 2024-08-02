FROM eclipse-temurin:17-jdk-jammy

WORKDIR /bot

COPY SlimeBot.jar ./SlimeBot.jar

WORKDIR /bot/run
ENTRYPOINT ["java", "-jar", "../SlimeBot.jar"]