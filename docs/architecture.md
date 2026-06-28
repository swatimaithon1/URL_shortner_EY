# Architecture Overview

## Components

- `UrlShortenerController`
  - CRUD and analytics API for short URLs
- `RedirectController`
  - public redirect endpoint (`GET /{code}`)
- `DefaultShortUrlService`
  - core business logic, expiration checks, analytics tracking, cleanup scheduling
- `JdbcShortUrlRepository`
  - JDBC-backed repository for short URL storage and analytics counters
- `Flyway` migrations
  - versioned schema management in `src/main/resources/db/migration`
- `ShortCodeGenerator`
  - secure random Base62 short code generation
- `UrlSafetyValidator`
  - validates URL structure/scheme and blocks localhost/private targets
- `ApiExceptionHandler`
  - centralized mapping of errors to consistent JSON response

## Control flow

1. Client calls `POST /api/v1/urls`.
2. Service validates URL and optional alias.
3. Service generates/stores short code and returns metadata.
4. Client calls `GET /{code}`.
5. Service resolves record, rejects expired links, tracks click analytics, returns redirect target.
6. Client can query `GET /api/v1/urls/{code}/analytics` for usage insights.

## Key decisions

- JDBC + Flyway for durable local persistence and forward-compatible schema evolution.
- Separate API and redirect controllers to keep responsibilities explicit.
- Centralized validation + exception handler to keep controllers small and predictable.
- Scheduled cleanup to reduce stale data and enforce expiration policy.

## Quality gates used

- Unit/integration tests with `MockMvc`
- Input validation via Bean Validation (`jakarta.validation`)
- Defensive URL safety checks to reduce SSRF risk

