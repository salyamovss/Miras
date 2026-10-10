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

- `catalog` — materials, their localized metadata, and authors.
- `identity` — administrator accounts and the `ADMIN` / `SUPER_ADMIN` roles.
- `publication` — review and approval workflow.
- `media` — metadata for uploaded files and their language. S3 integration is not
  configured yet.

These are application modules in one deployable application, not separate
services. Module ownership and current domain decisions are documented in
[`docs/backend-foundation.md`](docs/backend-foundation.md).

## Database

The application reads its PostgreSQL connection from `DB_URL`, `DB_USERNAME`,
and `DB_PASSWORD`. Local defaults are provided in `application.yaml`.
Liquibase changesets live in `src/main/resources/db/changelog/`.

## Run

Requires JDK 21 and a PostgreSQL database. Maven is included through the project
wrapper.

```bash
./mvnw spring-boot:run
```
