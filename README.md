# Anime and Manga Tracker Backend

Kotlin Spring Boot API for the Anime and Manga Tracker.

Frontend repository:
[CST438-FA26-Team6/Project3](https://github.com/CST438-FA26-Team6/Project3)

For detailed verification and troubleshooting steps, see
[`docs/runbook.md`](docs/runbook.md).

## Requirements

To run the local stack:

- Git
- Docker Desktop with Docker Compose

To run the tests directly:

- Java 21
- Docker Desktop, because the integration tests use Testcontainers

Gradle does not need to be installed. The repository includes the Gradle
wrapper.

## Clone and configure

```bash
git clone https://github.com/CST438-FA26-Team6/Project3-backend.git
cd Project3-backend
cp .env.example .env
```

The example environment file contains development-only values that are safe
for a local Docker stack. Do not reuse them in production.

Google and GitHub OAuth credentials are optional for starting the API and
running the automated tests. Leave those values blank unless the team gives
you test credentials privately. Never commit a populated `.env` file or real
credentials.

## Start the API and PostgreSQL

Make sure Docker Desktop is running, then run:

```bash
docker compose up --build
```

The first build can take several minutes. When startup finishes, open a second
terminal and verify the API:

```bash
curl http://localhost:8080/health
```

Expected response:

```json
{"status":"UP"}
```

The API uses port `8080`, and PostgreSQL uses port `5432`.

## Load sample titles

After the API has started and created the tables, run:

```bash
docker compose exec -T db sh -c \
  'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < scripts/seed.sql
```

The seed script is safe to run more than once. Confirm the rows with:

```bash
docker compose exec db sh -c \
  'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT id, title, media_type, total_episodes_chapters FROM titles ORDER BY title;"'
```

## Run the tests

Keep Docker Desktop running, then run:

```bash
./gradlew clean check
```

On Windows PowerShell, use:

```powershell
.\gradlew.bat clean check
```

The command runs the full test suite and verifies the 70% service-line
coverage requirement. The HTML coverage report is generated at
`build/reports/jacoco/test/html/index.html`.

## Useful Docker commands

Run in the background:

```bash
docker compose up --build -d
```

Check container status:

```bash
docker compose ps
```

Follow API logs:

```bash
docker compose logs -f api
```

Stop the stack while preserving database data:

```bash
docker compose down
```

Delete the local containers and database volume:

```bash
docker compose down -v
```

The final command permanently deletes only the local Docker database volume.
