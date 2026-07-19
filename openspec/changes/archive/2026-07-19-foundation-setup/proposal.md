## Why

The backend and frontend exist as bare scaffolds with no database, migrations, API documentation, or frontend architecture patterns. Every feature (auth, shows, watch tracking) depends on this shared infrastructure — without it, each subsequent change would reinvent setup or accumulate inconsistent patterns.

## What Changes

- Add root `docker-compose.yml` for local PostgreSQL
- Configure Spring Boot with `local` profile, JPA, Flyway, springdoc OpenAPI, and structured error handling
- Rename backend root package from `org.example` to `com.tvtracker`
- Add Gradle tooling: Spotless, Checkstyle, and required dependencies (JPA, Postgres, Flyway, springdoc)
- Add Flyway initial migration creating the `users` table only
- Set up frontend routing (React Router), server state (TanStack Query), and app shell layout
- Add frontend dependencies and remove default Vite starter content

## Capabilities

### New Capabilities

None — this change is infrastructure only; no new user-facing behavior is introduced.

### Modified Capabilities

None — existing domain specs in `openspec/specs/` are unchanged.

## Non-goals

- Implementing authentication endpoints or UI (Phase 1, separate change)
- Creating show, watch, review, or other domain tables beyond `users`
- Production deployment, CI/CD, or containerizing the app itself
- TMDb integration or any external API wiring

## Impact

- **Backend**: New package layout under `com.tvtracker`, new config/common modules, Gradle dependency additions, Flyway migrations directory
- **Frontend**: New folder structure (`routes/`, `lib/`, `components/`), dependency additions, replacement of starter `App.tsx`
- **Root**: New `docker-compose.yml`
- **Dependencies**: PostgreSQL (Docker), springdoc-openapi, Flyway, react-router-dom, @tanstack/react-query
