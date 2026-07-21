# Shogunate

A personal TV show tracker. Search shows via [TVmaze](https://www.tvmaze.com), build a private library, and mark episodes, seasons, and shows as watched—with cascade support and a full watch history for future analytics.

## Features

**Implemented**

- Username + password authentication (JWT)
- TVmaze show search and add-to-library (shared global catalog, per-user library entries)
- Library statuses: `None`, `Plan to Watch`, and `Watched` (set automatically when every episode is watched)
- Watch / unwatch at episode, season, or show level, with cascade and confirmation for destructive unmarks
- Season progress on show detail pages

**Planned** (see [`openspec/TASKS.md`](openspec/TASKS.md))

- Reviews and ratings (episode, season, show)
- Favorites
- Watch analytics

## Tech stack

| Layer | Stack |
|-------|--------|
| Frontend | React 19, TypeScript, Vite, TanStack Query, React Router |
| Backend | Spring Boot 4, Java 21, Gradle, JWT, Flyway |
| Database | PostgreSQL 16 |
| External API | TVmaze (metadata only; proxied through the backend) |

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) (for local Postgres)
- [Java 21](https://adoptium.net/)
- [pnpm](https://pnpm.io/)

## Quick start

### 1. Start the database

```bash
docker compose up -d
```

Postgres listens on **localhost:5433** (credentials in [`docker-compose.yml`](docker-compose.yml)).

### 2. Run the backend

```bash
cd backend
./gradlew bootRun
```

API: http://localhost:8080  
Swagger UI: http://localhost:8080/swagger-ui.html

### 3. Run the frontend

```bash
cd frontend
pnpm install
pnpm dev
```

App: http://localhost:5173 (proxies API calls to `http://localhost:8080` by default)

Optional: copy [`frontend/.env.example`](frontend/.env.example) to `frontend/.env` if you need a different API URL.

### 4. Seed a test user (optional)

With Postgres running (backend may be up or down):

```bash
./scripts/populate_test_data.sh
```

Default credentials:

- **Username:** `firstmock_lastmock`
- **Password:** `TestPassword123!`

## Development

Backend and frontend are independent projects—run commands from each directory.

### Backend

```bash
cd backend
./gradlew build          # compile + test
./gradlew test           # tests only
./gradlew bootRun        # run locally
./gradlew spotlessApply  # format
./gradlew checkstyleMain checkstyleTest
```

### Frontend

```bash
cd frontend
pnpm dev
pnpm build
pnpm exec vitest run
pnpm lint
pnpm format
```

## Project layout

```
backend/          Spring Boot REST API (feature-based packages: auth, show, watch, …)
frontend/         React SPA (features mirror backend domains)
openspec/         Spec-driven development: live specs, roadmap, change archive
docs/             PRD, architecture notes, historical planning docs
scripts/          Local dev helpers
docker-compose.yml
```

Behavior is defined in [`openspec/specs/`](openspec/specs/). Implementation detail and package structure live in [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## API overview

| Area | Endpoints |
|------|-----------|
| Auth | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/me` |
| Library | `GET /api/shows/search`, `POST /api/shows`, `GET/PATCH/DELETE /api/shows/{id}` |
| Watch | `POST/DELETE /api/watch/{episodes\|seasons\|shows}/{id}` |

All library and watch routes require a valid JWT (`Authorization: Bearer …`).

## Contributing / workflow

This repo uses [OpenSpec](openspec/AGENTS.md) for spec-driven changes:

1. Propose a change under `openspec/changes/`
2. Implement against the change tasks and delta specs
3. Archive when complete (merges specs into `openspec/specs/`)

Phase-level progress is tracked in [`openspec/TASKS.md`](openspec/TASKS.md).

## Attribution

Show metadata and images come from the [TVmaze API](https://www.tvmaze.com/api), licensed under [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/). See the in-app **About** page for details.

## License

Copyright (C) 2026 Ville Saloranta.

This project is licensed under the [GNU General Public License v3.0 or later](LICENSE.md).
Third-party software and data attributions are in [NOTICE.md](NOTICE.md).
