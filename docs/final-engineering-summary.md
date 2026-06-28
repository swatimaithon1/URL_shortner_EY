# Final Engineering Summary

## Plan and rationale

- Build a production-style prototype while optimizing for interview timeframe.
- Keep architecture modular (`api`, `service`, `repository`, `validation`, `exception`).
- Favor deterministic local execution with H2 JDBC storage and Flyway schema migrations.

## Artifacts produced

- Working Spring Boot URL shortener service with core APIs and redirect behavior.
- Analytics model (`totalClicks`, `lastAccessedAt`, `dailyClicks`).
- Reliability controls (expiration, scheduled cleanup, bounded code retries, URL safety checks).
- Integration + unit tests.
- Architecture and scenario documentation.

## Risks, trade-offs, and mitigations

- **Trade-off:** local H2 database is durable but not production-grade for HA or scaling.
  - **Mitigation:** repository abstraction remains in place for PostgreSQL migration.
- **Trade-off:** no auth/rate-limiting in prototype.
  - **Mitigation:** scoped as functional prototype; call out for next iteration.
- **Risk:** regex/path constraints may exclude desired aliases.
  - **Mitigation:** centralized validation to update policy safely.
- **Risk:** single-node cleanup and counters are not distributed.
  - **Mitigation:** document horizontal-scaling gap.

## Validation strategy

- `MockMvc` integration tests for key API flows and status codes.
- Unit test for short code generation constraints.
- Bean validation + exception mapping for predictable failures.

## Assumptions

- Prototype is acceptable with in-memory storage.
- Short links are public; no user authentication required in scope.
- Retention and analytics granularity are day-level only.

## Limitations

- Local H2 persistence only; no production RDBMS deployment automation.
- No API version negotiation beyond fixed `/api/v1` path.
- No observability stack (metrics/tracing/log aggregation).

## Suggested next iteration

1. Replace in-memory repository with PostgreSQL + Flyway.
2. Add authentication, tenant isolation, and per-tenant quotas.
3. Add rate limiting and abuse detection.
4. Emit metrics (`/actuator`) and distributed tracing.
5. Add contract tests and performance benchmarks.

