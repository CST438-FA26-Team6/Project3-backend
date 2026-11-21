# Backend Onboarding Runbook

This runbook is for a visitor testing the backend on a new laptop. It assumes
only the files and instructions in this repository are available.

## 1. Prerequisites

Install and start:

- Git
- Docker Desktop with Docker Compose
- Java 21 for running the Gradle test suite

Check the tools:

```bash
git --version
docker --version
docker compose version
java -version
```

The Java output must report version 21. Gradle itself does not need to be
installed.

## 2. Clone both default branches

The two repositories must be sibling folders under one parent directory:

```bash
mkdir Project3-workspace
cd Project3-workspace
git clone https://github.com/CST438-FA26-Team6/Project3.git
git clone https://github.com/CST438-FA26-Team6/Project3-backend.git
cd Project3-backend
git branch --show-current
```

Both repositories should be on `main`. Keep this folder layout because the
frontend instructions use the same shared workspace.

## 3. Create the local configuration

```bash
cp .env.example .env
```

The provided values are only for local development. The API and tests do not
need real Google or GitHub credentials. OAuth login will not work while the
four provider values are blank.

Do not put production passwords, OAuth secrets, or other real credentials in
the repository.

## 4. Start the local stack

```bash
docker compose up --build
```

Leave this terminal running. Startup is complete when the logs report that
the Spring Boot application has started without an error. In another terminal,
run:

```bash
docker compose ps
curl http://localhost:8080/actuator/health
```

Both `api` and `db` should be running. The health request should return:

```json
{"status":"UP"}
```

Open `http://localhost:8080/swagger-ui.html` to view and try the API
documentation. The source specification is available at
`http://localhost:8080/openapi.yaml`.

## 5. Load and verify sample data

Run the seed after the API has created its database tables:

```bash
docker compose exec -T db sh -c \
  'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < scripts/seed.sql
```

Verify the titles:

```bash
docker compose exec db sh -c \
  'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT id, title, media_type, total_episodes_chapters FROM titles ORDER BY title;"'
```

The output should contain the sample anime and manga from `scripts/seed.sql`.
The script skips records that already exist.

## 6. Run the test suite

Keep Docker Desktop running because the integration tests start temporary
PostgreSQL containers with Testcontainers.

macOS or Linux:

```bash
./gradlew clean check
```

Windows PowerShell:

```powershell
.\gradlew.bat clean check
```

A successful run ends with `BUILD SUCCESSFUL`. It also enforces the 70%
service-line coverage rule. The HTML coverage report is located at:

```text
build/reports/jacoco/test/html/index.html
```

## 7. Stop or reset the stack

Stop the containers and preserve the database:

```bash
docker compose down
```

Reset all local database data:

```bash
docker compose down -v
```

The `-v` command permanently deletes the local Docker database volume. It
does not affect Supabase or Render.

## Troubleshooting

### Docker is not running

Start Docker Desktop and wait until it reports that the engine is running.
Then repeat the command.

### Port 8080 or 5432 is already in use

Stop the other local service, or change `API_PORT` or `POSTGRES_PORT` in the
local `.env` file before starting Compose again.

### The API container exits

View its logs:

```bash
docker compose logs api
```

Confirm that `.env` exists and was copied from `.env.example`.

### Tests cannot find Docker

Start Docker Desktop, confirm `docker ps` succeeds, and rerun the tests.

### Gradle cannot run

Confirm `java -version` reports Java 21. On macOS or Linux, also run:

```bash
chmod +x gradlew
```

## Visitor command log

Record every command, its result, and any unclear instruction while testing.

| Command | Result | Notes or error |
| --- | --- | --- |
| `git clone .../Project3.git` |  |  |
| `git clone .../Project3-backend.git` |  |  |
| `cp .env.example .env` |  |  |
| `docker compose up --build` |  |  |
| `curl http://localhost:8080/actuator/health` |  |  |
| `./gradlew clean check` |  |  |
