# BookManagement

Spring Boot service that owns the book catalog (`book_detail`, `book_copy`).

**Maven:** `com.kopibru:BookManagement`  
**Port:** `8081`  
**Database:** `book_catalogue`

IndexShelf calls these APIs over HTTP for list / borrow / return (it no longer accesses `book_copy` directly).

## Run

### Option A — Local (Maven)

```bash
# Dev profile (default) — requires local PostgreSQL
mvn spring-boot:run

# Prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Option B — Docker Compose (app + Postgres)

```bash
docker compose up --build
```

This starts:

- PostgreSQL on host port `5433` (container still uses `5432`)
- BookManagement API on port `8081` (`prod` profile)

Stop with `docker compose down` (add `-v` to also remove the DB volume).

Base URL: `http://localhost:8081`

### Port and profile (local vs GCP)

| Setting | Local default | Production / GCP |
|---------|---------------|------------------|
| Port | `8081` (`PORT` unset) | Set env `PORT` (Cloud Run sets this, often `8080`) |
| Profile | `dev` | Set env `SPRING_PROFILES_ACTIVE=prod` |

`application.properties` uses:

```properties
server.port=${PORT:8081}
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}
```

On GCP (Cloud Run), configure env vars such as:

- `SPRING_PROFILES_ACTIVE=prod`
- `CLOUD_SQL_INSTANCE=project:region:instance` (**required** — from Cloud SQL instance overview)
- `DB_NAME=book_catalogue` (optional; default)
- `DB_USERNAME` / `DB_PASSWORD`
- Attach the same Cloud SQL instance under Cloud Run **Cloud SQL connections**
- Grant the Cloud Run service account `roles/cloudsql.client`

Do **not** set `DB_URL` to `jdbc:postgresql://localhost:5432/...` on Cloud Run.
Do **not** bake `prod` into the JAR; override with env vars at deploy time.

### Cloud Run → Cloud SQL (recommended)

Prod JDBC default (when `DB_URL` is unset):

```text
jdbc:postgresql:///<DB_NAME>?cloudSqlInstance=<CLOUD_SQL_INSTANCE>
  &socketFactory=com.google.cloud.sql.postgres.SocketFactory
  &cloudSqlRefreshStrategy=lazy
```

Create the database in Cloud SQL Console before the first deploy. Local Docker Compose still sets `DB_URL=jdbc:postgresql://postgres:5432/book_catalogue`.

### CI/CD (GitHub Actions)

Declarative pipeline in `.github/workflows/ci.yml`:

1. On push/PR to `main`: run `mvn verify` (tests + coverage gate)
2. On push to `main`: build and publish the Docker image to GitHub Container Registry (`ghcr.io`)

Pull the published image (after a successful push to `main`):

```bash
docker pull ghcr.io/<your-github-username>/bookmanagement:latest
```

## APIs

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/api/books` | Register book copy |
| `POST` | `/api/books/from-file` | Import from `.txt` / `.dat` |
| `GET` | `/api/books?status=all\|available\|borrowed` | List books |
| `POST` | `/api/books/{bookId}/borrow` | Borrow (`{"libraryId":"..."}`) |
| `POST` | `/api/books/{bookId}/return` | Return (`{"libraryId":"..."}`) |

### Register book
```bash
curl -X POST http://localhost:8081/api/books \
  -H "Content-Type: application/json" \
  -d '{"id":"BOOK-001","isbn":"978-0134685991","title":"Effective Java","author":"Joshua Bloch"}'
```

### Import from file
```bash
curl -X POST http://localhost:8081/api/books/from-file \
  -F "file=@src/main/resources/sample-books.txt"
```

### Borrow / return (used by IndexShelf)
```bash
curl -X POST http://localhost:8081/api/books/BOOK-001/borrow \
  -H "Content-Type: application/json" \
  -d '{"libraryId":"LIB-001"}'
```
