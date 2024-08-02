FROM eclipse-temurin:17-jdk-jammy

WORKDIR /bot

COPY SlimeBot.jar ./SlimeBot.jar
ADD ./run/* ./

ENTRYPOINT ["java", "-jar", "SlimeBot.jar"]