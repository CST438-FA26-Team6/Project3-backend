# Anime and Manga Tracker Backend

Kotlin Spring Boot API for the Anime and Manga Tracker.

## Requirements

- Docker Desktop
- Git

Java and Gradle do not need to be installed when running the project with Docker.

## First-time setup

1. Clone the repository and enter its folder:

   ```bash
   git clone https://github.com/CST438-FA26-Team6/Project3-backend.git
   cd Project3-backend
   ```

2. Create your local environment file:

   ```bash
   cp .env.example .env
   ```

3. Open `.env` and replace `replace-with-a-local-password` with a local database password. Never commit `.env`.

4. Make sure Docker Desktop is running.

## Start the API and PostgreSQL

```bash
docker compose up --build
```

Docker Compose will:

- Build and start the Kotlin Spring Boot API
- Start PostgreSQL 17
- Wait for PostgreSQL to become healthy before starting the API
- Preserve local database data in a Docker volume

The API is available at [http://localhost:8080](http://localhost:8080). PostgreSQL is available on port `5432`.

The API currently returns `404` at the root URL because no controllers have been added yet. This is expected.

## Run in the background

```bash
docker compose up --build -d
```

View running services:

```bash
docker compose ps
```

View API logs:

```bash
docker compose logs -f api
```

## Stop the services

```bash
docker compose down
```

This stops the containers but preserves the PostgreSQL data.

To stop the containers and permanently delete the local database volume, run:

```bash
docker compose down -v
```

## Run the tests without Docker Compose

```bash
./gradlew test
```
