@echo off
setlocal

REM Копирование файла на удаленный сервер
scp "C:\Users\Gleb\IdeaProjects\MeetSchedulerBot\target\MeetSchedulerBot.jar" username@77.232.128.56:/home/username/Docker/MSB

REM Остановка контейнера MeetSchedulerBot
ssh username@77.232.128.56 docker stop MeetSchedulerBot

REM Удаление контейнера MeetSchedulerBot
ssh username@77.232.128.56 docker rm MeetSchedulerBot

REM Выполнение docker-compose.yml на удаленном сервере
ssh username@77.232.128.56 docker-compose -f /home/username/Docker/MSB/docker-compose.yml up -d

endlocal