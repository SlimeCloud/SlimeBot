FROM eclipse-temurin:21-jdk-jammy

WORKDIR /bot

COPY SlimeBot.jar ./SlimeBot.jar
EXPOSE 3737

WORKDIR /bot/run
ENTRYPOINT ["java", "-jar", "../SlimeBot.jar"]