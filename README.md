
# JIrcBot
__powered by [PircBotX](https://github.com/pircbotx/pircbotx)__

Из реализованных функций:

1. Пинг
2. Приветствие входящих пользователей
3. Калькулятор с возможностью вычислять выражения с переменными
4. Проверка регулярных выражений
5. Выдача советов от сервиса [fucking-great-advice](http://fucking-great-advice.ru/api/random)
6. Автопостинг названия URL при его появлении в чате
7. Викторины - предоставлены сервисом [baza-otvetov.ru](https://baza-otvetov.ru)
8. Выдача случайной цитаты из сервиса [Bash.Org / Bash.im](bash.im/random)
9. Авторелогин после кика. Бывают ситуации, когда другой бот автоматически кикает моего бота (в канале #lor на freenode, например).
10. Система отложенных сообщений. Можно написать `?tell <username|me> your phrase here`, и бот доставит это сообщение указанному пользователю, когда он напишет в чат
11. Грамматический контроль и коррекция
12. Поиск Google
_____

### 2018-03-29

* Добавлена возможность администрирования бота через приватные сообщения к нему

### 2018-03-28

* Портирован на SpringBoot 2 (__NB!__ _перед запуском бота версии 2.0.0 необходимо запустить скрипт в базе: /srv/main/resources/sql/update_to_2.0.0.sql_)

### 2018-03-27

* Добавлена возможность коннекта бота к нескольким серверам

### 2018-02-06 

* Добавлен поиск Google

### 2018-02-05

* Добавлен модуль грамматического контроля и коррекция

### 2018-02-01

* К боту подключена БД (сейчас PostgreSQL). 
* Реализованы индивидуальные настройки возможностей бота для каждого канала - хранится в БД
* Реализована система отложенных сообщений

_____

## Установка и запуск

### Требования для работы:

1. JDK версии 1.8
2. Maven
3. PostgreSQL версии 9.4-9.6 (на других не проверялся) - настройки подключения находятся в файле `\src\main\resources\application.yml`

По-умолчанию PostgreSQL расположен на localhost, должен быть создан пользователь `jircbot` с паролем `jircbotpass`, и он должен быть владельцем базы `jircbot`.


### Установка

#### Клонируем репозиторий

```bash

> git clone https://github.com/bvn13/JIrcBot.git

Cloning into 'JIrcBot'...
remote: Counting objects: 615, done.
remote: Compressing objects: 100% (199/199), done.
Receiving objects:  83% (511/615), 76.01 KiB | 65.00), pack-reused 167Receiving objects:  82% (505/615), 76.01 KiB | 65.00 KiB/s
Receiving objects: 100% (615/615), 111.97 KiB | 72.00 KiB/s, done.
Resolving deltas: 100% (253/253), done.

```

#### Собираем проект

```bash

> cd JIrcBot

> mvn clean package

[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building jircbot 1.1.2
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ jircbot ---
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ jircbot ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 2 resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ jircbot ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 41 source files to C:\dev\test\JIrcBot\target\classes
[WARNING] /C:/dev/test/JIrcBot/src/main/java/ru/bvn13/jircbot/config/JircBotConfiguration.java: C:\dev\test\JIrcBot\src\main\java\ru\bvn13\jircbot\config\JircBotConfiguration.java uses unchecked or unsafe operations.
[WARNING] /C:/dev/test/JIrcBot/src/main/java/ru/bvn13/jircbot/config/JircBotConfiguration.java: Recompile with -Xlint:unchecked for details.
[INFO]
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ jircbot ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory C:\dev\test\JIrcBot\src\test\resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ jircbot ---
[INFO] No sources to compile
[INFO]
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ jircbot ---
[INFO] No tests to run.
[INFO]
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ jircbot ---
[INFO] Building jar: C:\dev\test\JIrcBot\target\jircbot-1.1.2.jar
[INFO]
[INFO] --- spring-boot-maven-plugin:1.5.6.RELEASE:repackage (default) @ jircbot ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 17.916 s
[INFO] Finished at: 2018-02-08T10:35:43+03:00
[INFO] Final Memory: 33M/287M
[INFO] ------------------------------------------------------------------------

```

#### Настройка

В файле config.json в корне проекта хранятся настройки. Укажите сервер, имя бота и каналы, на которые он должен заходить.
```json

{
  "version" : "1.0",
  "connections" : [
    {
      "enabled" : true,
      "server" : "irc.freenode.net",
      "port" : 6667,
      "channelsNames" : ["#voidforum", "#lor"],
      "botName" : "jircbot"
    },
    {
      "enabled" : true,
      "server" : "irc.mozilla.org",
      "port" : 6667,
      "channelsNames" : ["#voidforum", "#lor"],
      "botName" : "jircbot"
    }
  ]
}

```


#### Запуск
 
Для Linux можно использовать модуль SystemD, который лежит в корне проекта. Конфигурационный файл должен лежать рядом с файлом .jar

```

[Unit]
Description=JIrcBot
After=network.target

[Service]
User=bvn13
WorkingDirectory=/srv/jircbot
ExecStart=/usr/bin/java -jar /srv/jircbot/jircbot-1.1.2.jar
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target

```


В противном случае можно использовать запуск из командной строки.

```bash

> java -jar target/jircbot-1.1.2.jar

```
