# AGENTS.md

Guidance for AI coding agents working in this repository.

## Project Overview

- **Backend**: Java, Spring Boot
- **Frontend**: React, TypeScript
- **Database**: PostgreSQL

Update the paths below to match your actual repo layout.

```
/backend    -> Spring Boot application (Java, Gradle)
/frontend   -> React + TypeScript application
```

## Setup Commands

### Backend
```bash
cd backend
./gradlew build
./gradlew bootRun
```

### Frontend
```bash
cd frontend
pnpm install
pnpm dev
```

## Code Style

### Backend (Java / Spring Boot)
- Follow standard Java naming conventions (PascalCase classes, camelCase methods/vars).
- Package by feature (e.g. `com.company.app.user`), not by layer, unless the existing codebase already uses a layer-first structure — match what's there.
- Use constructor injection for dependencies, not field injection (`@Resource` on fields).
- Keep controllers thin: validation and request/response mapping only. Business logic belongs in services.
- Use DTOs for API request/response bodies; do not expose JPA entities directly over the wire.

### Frontend (React / TypeScript)
- Strict TypeScript: avoid `any`; prefer explicit types/interfaces for props and API responses.
- Functional components with hooks; no class components.
- Co-locate component, styles, and tests (e.g. `Button.tsx`, `Button.test.tsx`).
- Keep API calls in a dedicated `services/` or `api/` layer, not inline in components.
- Prefer named exports over default exports for consistency (adjust if the repo already uses default exports).

### Database (PostgreSQL)
- All schema changes go through migrations (e.g. Flyway) — never edit the schema by hand in production.
- Migration files are append-only: don't edit a migration that has already been applied/merged; add a new one.
- Use snake_case for table and column names.



## Pull Request / Commit Guidelines

- Keep commits focused; one logical change per commit.
- Write commit messages in imperative mood (e.g. "Add user repository", not "Added" or "Adding").
- Reference the relevant ticket/issue number if applicable.
- Do not commit `.env` files, secrets, or local `application-local.yml` overrides.

## Security & Config Notes

- Secrets (DB credentials, API keys, JWT secrets) come from environment variables or a secrets manager — never hardcoded, never committed.
- CORS configuration for the frontend-backend connection lives in the backend's `WebConfig`/`SecurityConfig` — update there, not by disabling CORS checks in the browser or proxying around it.
- Validate all input at the API boundary (Spring `@Valid` / Bean Validation on DTOs).

## Things Agents Should NOT Do

- Don't modify already-applied database migrations — add a new migration instead.
- Don't add new dependencies (Maven/Gradle or npm) without checking for an existing equivalent already in use.
- Don't bypass the DTO layer to expose JPA entities directly in API responses.
- Don't disable TypeScript strict mode or add blanket `// @ts-ignore` to silence errors — fix the underlying type issue.
- Don't run destructive database commands against anything other than a local/dev database.

## Notes for Agents

- If a section above doesn't match reality (e.g. you use Gradle not Maven, or Flyway not Liquibase), correct this file rather than guessing silently in future changes — this file should stay accurate.
- Prefer editing existing files/patterns over introducing new ones; match the conventions already present in the surrounding code even where they differ from the defaults above.