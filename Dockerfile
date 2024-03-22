# Используем базовый образ Java
FROM openjdk:17-jdk-alpine

# Копируем JAR файл в контейнер
COPY target/MeetSchedulerBot.jar /app/MeetSchedulerBot.jar

# Указываем рабочую директорию
WORKDIR /app

# Команда для запуска приложения при старте контейнера
CMD ["java", "-jar", "MeetSchedulerBot.jar"]
