## Context

Backend is a minimal Spring Boot 4.1 app in `org.example` with web + validation only. Frontend is the default Vite React template. No PostgreSQL, Flyway, OpenAPI, routing, or TanStack Query. Architecture docs specify `com.tvtracker` package layout, feature-based packages, and local dev via Docker Compose.

## Goals / Non-Goals

**Goals:**
- Runnable local dev stack: Postgres (Docker) + Spring Boot + React dev server
- Backend serves OpenAPI docs and structured JSON errors
- Flyway manages schema; initial migration creates `users` table only
- Frontend has Router + QueryClient + minimal app shell
- Code quality tooling (Spotless, Checkstyle) wired in Gradle

**Non-Goals:**
- Auth business logic, JWT, or security filter
- Domain feature packages beyond `common/` and `config/`
- Vitest setup (deferred to feature changes)

## Decisions

### Package rename: `org.example` → `com.tvtracker`
Aligns with ARCHITECTURE.md. Main class becomes `com.tvtracker.TvTrackerApplication`.

### Spring profile: `local` as default for dev
`application.yml` sets default profile to `local`; `application-local.yml` holds DB credentials matching docker-compose.

### Flyway on startup
Migrations run automatically via Spring Boot Flyway integration. Initial migration: `V1__create_users_table.sql`.

### springdoc-openapi
Use `springdoc-openapi-starter-webmvc-ui` for Swagger UI at `/swagger-ui.html`.

### Error handling
`GlobalExceptionHandler` maps `ApiException` subclasses to JSON `{ "message": "...", "status": N }`. A health/demo endpoint throws `ApiException` in tests to verify behavior.

### Frontend routing
React Router v7 with routes: `/` (home shell), placeholder for future auth routes. Layout component with nav stub.

### TanStack Query v5
Single `QueryClient` in `lib/queryClient.ts`, wrapped in `QueryClientProvider` at app root.

### Docker Compose
Postgres 16 on port 5432, database `shogunate`, user/password for local dev only.

## Risks / Trade-offs

- **[Risk] JWT secret and DB creds in local config** → Acceptable for local-only; document that production config is out of scope
- **[Risk] Users table created before auth feature** → Intentional per TASKS.md 0.5; auth change adds token tables
- **[Risk] Package rename breaks existing references** → Only one Main.java exists; low impact

## Migration Plan

1. Add docker-compose and start Postgres
2. Update Gradle deps and rename package
3. Add config, exception handler, Flyway migration
4. Verify `./gradlew bootRun` connects and Swagger loads
5. Update frontend deps and shell
6. Verify `pnpm dev` shows routed layout

No rollback needed — greenfield setup.

## Open Questions

None — all decisions follow existing ARCHITECTURE.md and TASKS.md Phase 0.
