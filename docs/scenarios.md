# Scenario Execution (Greenfield, Brownfield, Ambiguous)

## 1) Greenfield Scenario: Build URL shortener from scratch

### Requirement slice
- Provide create/redirect APIs with analytics and expiration.

### Decomposition
- Define domain model and DTOs.
- Implement service + repository + short code generation.
- Expose REST APIs and redirect endpoint.
- Add tests and docs.

### Execution
- Implemented `ShortUrl` domain and in-memory repository.
- Implemented `ShortUrl` domain with JDBC repository backed by Flyway-managed schema.
- Added create/get/delete/analytics APIs and redirect flow.
- Added scheduled expiration cleanup.

### Validation
- Integration test verifies create -> redirect -> analytics click increment.
- Unit test verifies short code length and alphabet.

## 2) Brownfield Scenario: Improve generated Spring starter project

### Requirement slice
- Existing scaffold had no business logic and minimal test.

### Decomposition
- Replace placeholder test with meaningful API tests.
- Add validation/security config and error responses.
- Add runtime configuration in `application.properties`.

### Execution
- Updated dependencies in `pom.xml` (validation + starter test).
- Added `SecurityConfig` to avoid default lock-down for local API tests.
- Added global exception handler and typed domain exceptions.

### Validation
- Duplicate alias conflict test (`409`) added.
- Invalid localhost URL safety test (`400`) added.

## 3) Ambiguous Scenario: "Add reliability features"

### Ambiguities identified
- Reliability could mean persistence, retries, observability, and abuse control.
- No explicit non-functional targets (RPS, latency, retention period, SLA).

### Chosen interpretation for prototype scope
- Collision retries for code generation.
- Expiration lifecycle controls + scheduled cleanup.
- URL safety checks to prevent common SSRF abuse targets.
- Uniform API error payloads for operational consistency.

### Validation and guardrails
- Guardrail: bounded generation attempts to avoid endless loops.
- Guardrail: expired links return `410 Gone` and are removed.
- Guardrail: localhost/private-network URLs rejected before persistence.

## AI-assisted traceability

| Item | AI usage | Engineer decision |
|---|---|---|
| Initial API/resource design | AI generated draft endpoint structure | Kept, but constrained alias regex and response shape |
| URL safety approach | AI suggested URI parsing and host checks | Refined to block localhost/private ranges without DNS dependency |
| Error handling | AI drafted generic exception mapping | Added typed exceptions and specific status-code mapping |
| Test generation | AI drafted starter tests | Edited assertions for redirect behavior and analytics correctness |
| Persistence upgrade | Add JDBC + Flyway migration path | Accepted to improve reliability and brownfield readiness |

