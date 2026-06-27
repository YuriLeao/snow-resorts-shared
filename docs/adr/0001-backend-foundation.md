# ADR 0001 — Backend foundation decisions

Status: Accepted · Date: 2026-06

## Context
Bootstrapping the Snow Resorts backend monorepo (5 selective microservices + shared libs)
per the architecture plan, targeting Java 25 and Spring Boot 3.4+.

## Decisions

1. **Spring Boot 3.5.6 (not 3.4.x).** The plan says "Spring Boot 3.4+". We pin 3.5.6 because
   Java 25 (class file v69) requires the newer Spring Framework 6.2.x / ASM bundled with Boot
   3.5.x; Boot 3.4 predates official Java 25 support. The version lives in the parent
   `<parent>` so it is trivial to bump.

2. **No Lombok.** Lombok's annotation processor historically lags new JDKs and can break the
   build on Java 25. We use explicit constructors (constructor injection), Java `record`s for
   DTOs/value objects, and explicit SLF4J logger fields. This satisfies the Java rule's
   "explicit constructor" allowance.

3. **Top-level Maven aggregator.** Root `pom.xml` inherits `spring-boot-starter-parent` and
   aggregates `shared/*` + `services/*`, giving one-command `mvn install` and centralised
   dependency/plugin management (Testcontainers, AWS SDK, springdoc, hibernate-spatial BOMs).

4. **One database, schema per service.** A single `snow_resorts` Postgres+PostGIS database with
   `auth`/`users`/`resorts`/`location`/`activity` schemas; each service owns its Flyway
   migrations. No cross-schema JOINs (communicate via API/events).

5. **Shared `security-lib` via Spring auto-configuration.** Adding the dependency wires the
   RFC 7807 handler, OWASP security-headers + correlation-id filters, and a default stateless
   JWT resource-server `SecurityFilterChain` (overridable per service via `@ConditionalOnMissingBean`).

6. **JWT: RS256 + JWKS.** `auth-service` issues RS256 tokens and publishes `/.well-known/jwks.json`;
   all other services validate as OAuth2 resource servers using that JWKS URI. Refresh tokens are
   opaque, single-use (rotation), stored only as SHA-256 hashes, with reuse-detection.

7. **Ports & Adapters by profile.** Object storage uses an `ObjectStorage` port with an
   S3-compatible adapter pointed at MinIO (`local`) or S3 (`aws`); cache/pub-sub uses Redis.

## Consequences
- Local dev is $0 (Docker Compose + JVM). AWS only for staging/prod (owned by the Terraform
  workstream). Integration tests use Testcontainers (Postgres/PostGIS, Redis) and run in CI.
