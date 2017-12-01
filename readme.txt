A simple chat app made with Spring Boot.

Run locally with:
mvn spring-boot:run

mysql commands used for local dev:
create database chatStorage;
create user 'chatApp'@'localhost' identified by 'chatPassword';
grant all on chatStorage.* to 'chatApp'@'localhost';

Some example requests:
curl -H "Content-Type: application/json" -X POST -d '{"username":"testUser","text":"whoa"}' localhost:8080/chat/
curl localhost:8080/chats/testUser
curl localhost:8080/chat/1


My endpoints were hosted on AWS with elastic beanstalk and RDS:
http://matthew-somers.com/chat
http://matthew-somers.com/chats/someUsername
http://matthew-somers.com/chat/1
