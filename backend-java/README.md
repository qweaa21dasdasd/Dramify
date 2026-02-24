# Drama Generator Backend (Java)

This is a Java implementation of the Drama Generator backend, built with Spring Boot.

## Prerequisites

- Java 17 or later
- Maven 3.6 or later

## Project Structure

- `src/main/java/com/dramagenerator/backend/model`: Entity classes (JPA)
- `src/main/java/com/dramagenerator/backend/repository`: Data access layer
- `src/main/java/com/dramagenerator/backend/service`: Business logic
- `src/main/java/com/dramagenerator/backend/controller`: REST API endpoints
- `src/main/java/com/dramagenerator/backend/dto`: Data Transfer Objects

## Configuration

Configuration is located in `src/main/resources/application.yml`.
By default, it uses a SQLite database located at `data/drama.db`.

## Running the Application

```bash
mvn spring-boot:run
```

## Building the Application

```bash
mvn clean package
```

The executable JAR will be created in `target/backend-0.0.1-SNAPSHOT.jar`.

## Implemented Features

- **Drama Management**: Create, read, update, delete dramas.
- **Character Management**: Manage characters for dramas and episodes.
- **Episode Management**: Manage episodes and scripts.
- **Image Generation**: Structure for image generation service (implementation pending full AI integration).
- **Task Management**: Async task tracking.

## Note on Java Version

This project uses Lombok. If you are using Java 21+ (e.g., Java 25), you might encounter compilation issues with Lombok. It is recommended to use Java 17 or Java 21 LTS.
