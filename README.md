# Miras Backend

Backend modular monolith for the Miras digital library. The public website and the
admin interface will be separate frontend applications that use this backend.

## Technology

- Java 21
- Spring Boot 4.1.1
- Spring Modulith 2.1.1
- Spring Data JPA
- PostgreSQL and Liquibase
- Maven Wrapper

## Modules

- `catalog` — materials, their localized metadata, authors, and reference data
  (countries).
- `identity` — administrator accounts and the `ADMIN` / `SUPER_ADMIN` roles.
- `publication` — review and approval workflow.
- `media` — metadata for uploaded files and their language. S3 integration is not
  configured yet.
- `notification` — notifications emitted when publication requests are decided.

These are application modules in one deployable application, not separate
services. Module ownership and current domain decisions are documented in
[`docs/backend-foundation.md`](docs/backend-foundation.md).

## Database

The application reads its PostgreSQL connection from the following environment
variables:

- `DB_URL` — JDBC URL (default `jdbc:postgresql://localhost:5432/miras`)
- `DB_USERNAME` — database username (default `postgres`)
- `DB_PASSWORD` — database password (no default)

Liquibase changesets live in `src/main/resources/db/changelog/`.

## Environment variables

A template with every supported variable is provided in
[`.env.example`](.env.example). It is not loaded automatically — copy it to
`.env` for your own records and export the variables (or configure them in your
IDE run configuration) before starting the application.

```bash
cp .env.example .env
# edit .env with your local credentials
```

## Before you run

1. Install JDK 21.
2. Start a PostgreSQL instance (local or Docker).
3. Create the application database:
   ```sql
   CREATE DATABASE miras;
   ```
4. Copy `.env.example` to `.env` and fill in `DB_PASSWORD` (and any other
   values that differ from the defaults).
5. Export the variables into your shell, for example:
   ```bash
   set -a && source .env && set +a
   ```

## Run

```bash
./mvnw spring-boot:run
```

## Test

```bash
./mvnw test
```
