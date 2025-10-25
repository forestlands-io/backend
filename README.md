# Forestlands Backend

Forestlands is a gamified focus (pomodoro) timer heavily inspired by Seekrtech's Forest app and Free Lives' Terra Nil game.
Each successful focus session produces a virtual tree that users can place on their isometric area map. The map visually evolves from wasteland into a blooming paradise, reflecting their accumulated focus.

## Stack

- Java 21, Spring Boot 3 (Monolith)
- MySQL 8, Hibernate, Flyway

---

## Getting Started (Local)

### Prerequisites
- Java 21
- MySQL 8
- Maven 3

### Start MySQL

**Option A: Docker**

```bash
docker run --name forestlands-mysql -p 3306:3306 \
  -e MYSQL_DATABASE=forestlands \
  -e MYSQL_USER=forestlands \
  -e MYSQL_PASSWORD=forestlands \
  -e MYSQL_ROOT_PASSWORD=root \
  -d mysql:8.0
```

** Option B: Local**
```
CREATE DATABASE IF NOT EXISTS `forestlands`;
CREATE USER IF NOT EXISTS 'forestlands'@'%' IDENTIFIED BY 'forestlands';
GRANT ALL PRIVILEGES ON `forestlands`.* TO 'forestlands'@'%';
```

### Run with local profile
```
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

For testing you can also use the `init-db` profile, which seeds database with initial data:
```
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,init-db
```
