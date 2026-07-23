# Architecture Document: TV Show Tracker

Companion to `PRD.md`. This document describes how the system is structured to deliver those requirements.

---

## 1. System Overview

A classic client-server architecture: a React SPA talks to a Spring Boot REST API over HTTPS/JSON, backed by PostgreSQL. The backend also talks out to TVmaze for show discovery/metadata. No other external services are involved for MVP.

```mermaid
flowchart LR
    subgraph Client
        FE[React + TypeScript SPA<br/>TanStack Query]
    end

    subgraph Server
        API[Spring Boot REST API]
        DB[(PostgreSQL)]
    end

    TVMAZE[(TVmaze API)]

    FE -- REST/JSON, JWT auth --> API
    API -- JDBC --> DB
    API -- HTTPS --> TVMAZE
```

- **Frontend** never talks to TVmaze directly — all metadata fetches are proxied through the backend, which owns catalog persistence on add.
- **Backend** is a single Spring Boot application (no separate microservices) — appropriate given the scope and the "local/dev only for now" deployment target.

---

## 2. Backend Architecture

### 2.1 Package Structure — Feature-Based

Each domain feature owns its full vertical slice (controller → service → repository → entity/dto). Cross-cutting concerns live in `common/` and `config/`.

```
backend/
└── src/main/java/com/tvtracker/
    ├── auth/
    │   ├── AuthController.java
    │   ├── AuthService.java
    │   ├── UserRepository.java
    │   ├── User.java                  (entity)
    │   └── dto/
    │       ├── RegisterRequest.java
    │       ├── LoginRequest.java
    │       └── AuthResponse.java
    │
    ├── show/
    │   ├── ShowController.java        (search + library CRUD)
    │   ├── ShowService.java
    │   ├── ShowRepository.java
    │   ├── SeasonRepository.java
    │   ├── EpisodeRepository.java
    │   ├── Show.java / Season.java / Episode.java   (global catalog entities)
    │   ├── UserLibrary.java / UserWatchState.java   (per-user library + watch state)
    │   ├── tvmaze/
    │   │   ├── TvmazeClient.java         (HTTP client to TVmaze)
    │   │   └── TvmazeMapper.java         (TVmaze DTOs -> local entities)
    │   └── dto/
    │
    ├── watch/
    │   ├── WatchController.java
    │   ├── WatchService.java           (orchestrates mark/unmark + downward cascade)
    │   ├── WatchHierarchySyncService.java  (upward cascade to season/show)
    │   ├── WatchEventRepository.java
    │   ├── WatchEvent.java             (entity — immutable log)
    │   └── dto/
    │
    ├── review/
    │   ├── ReviewController.java
    │   ├── ReviewService.java
    │   ├── ReviewRepository.java
    │   ├── Review.java
    │   └── dto/
    │
    ├── favorite/
    │   ├── FavoriteController.java
    │   ├── FavoriteService.java
    │   ├── FavoriteRepository.java
    │   ├── Favorite.java
    │   └── dto/
    │
    ├── analytics/
    │   ├── AnalyticsController.java
    │   ├── AnalyticsService.java       (reads across watch/review/favorite)
    │   └── dto/
    │
    ├── common/
    │   ├── TargetType.java             (shared enum: EPISODE/SEASON/SHOW)
    │   ├── exception/                  (ApiException, GlobalExceptionHandler)
    │   └── security/
    │       ├── JwtTokenProvider.java
    │       ├── SecurityConfig.java
    │       └── CurrentUserResolver.java
    │
    └── config/
        ├── OpenApiConfig.java          (Swagger/OpenAPI setup)
        └── TvmazeConfig.java             (base URL, User-Agent, rate-limit settings)
```

- `analytics/` is intentionally read-only and cross-cutting — it queries `watch_events`, `reviews`, and `favorites` directly rather than owning its own tables.
- `watch/` owns all cascade logic (Section 5) so that mark/unmark rules live in exactly one place.

### 2.2 API Documentation
OpenAPI/Swagger is auto-generated via `springdoc-openapi`, exposing:
- `/v3/api-docs` — raw OpenAPI JSON
- `/swagger-ui.html` — interactive docs UI

Every controller endpoint is annotated (`@Operation`, `@ApiResponse`) so the generated docs stay accurate without a hand-maintained spec.

---

## 3. Database Schema

```mermaid
erDiagram
    USERS ||--o{ USER_LIBRARY : has
    SHOWS ||--o{ USER_LIBRARY : referenced_by
    SHOWS ||--o{ SEASONS : has
    SEASONS ||--o{ EPISODES : has
    USERS ||--o{ USER_WATCH_STATE : tracks
    USERS ||--o{ REVIEWS : writes
    USERS ||--o{ WATCH_EVENTS : logs
    USERS ||--o{ FAVORITES : flags

    USERS {
        uuid id PK
        text username
        text password_hash
        timestamp created_at
    }
    SHOWS {
        uuid id PK
        int tvmaze_id UK
        text title
        text overview
        text poster_url
        text tvmaze_url
        date first_air_date
        timestamp created_at
    }
    SEASONS {
        uuid id PK
        uuid show_id FK
        int season_number
        text name
    }
    EPISODES {
        uuid id PK
        uuid season_id FK
        int episode_number
        text title
        date air_date
    }
    USER_LIBRARY {
        uuid id PK
        uuid user_id FK
        uuid show_id FK
        text library_status
        timestamp added_at
    }
    USER_WATCH_STATE {
        uuid user_id FK
        text target_type
        uuid target_id
        boolean watched
        timestamp watched_at
    }
    REVIEWS {
        uuid id PK
        uuid user_id FK
        text target_type
        uuid target_id
        numeric rating
        text body
        timestamp created_at
        timestamp updated_at "nullable; set by DB trigger on UPDATE"
    }
    WATCH_EVENTS {
        uuid id PK
        uuid user_id FK
        text target_type
        uuid target_id
        text action
        timestamp occurred_at
        boolean triggered_by_cascade
        uuid cascade_source_id FK
    }
    FAVORITES {
        uuid id PK
        uuid user_id FK
        uuid show_id FK
        timestamp created_at
    }
```

### 3.1 Notes on Types & Constraints
- `shows`, `seasons`, `episodes` are a **shared global catalog** keyed by `tvmaze_id` (unique on `shows`). No `user_id` on catalog tables.
- `user_library` links users to catalog shows: `library_status` is `'NONE' | 'PLAN_TO_WATCH'`.
- Current watched state lives in `user_watch_state` (per user, per catalog target) — not on catalog rows.
- `target_type` (on `reviews`, `watch_events`, `user_watch_state`): `'EPISODE' | 'SEASON' | 'SHOW'`.
- `favorites` stores show-only user choices: `(user_id, show_id)` unique.
- `rating` on `reviews`: `NUMERIC(2,1)`, application-validated to `{1.0, 1.5, 2.0, ..., 5.0}` (0.5 increments).
- `reviews.updated_at` is nullable on insert; a Postgres `BEFORE UPDATE` trigger sets it to `NOW()` — the application never assigns `updated_at`.
- `watch_events.action`: `'WATCHED' | 'UNWATCHED'`.
- `watch_events` is **append-only** during normal watch operations.
- Unique constraints: `user_library(user_id, show_id)`, `reviews(user_id, target_type, target_id)`, `favorites(user_id, show_id)`.
- Indexes: `user_library(user_id)`, `seasons(show_id)`, `episodes(season_id)`, `user_watch_state(user_id, target_type, target_id)`.
- Library access is scoped via `user_library` membership checks on every show detail/list query. Reviews, watch events, and favorites remain scoped by `user_id`. Catalog metadata is shared but never exposed without library membership.

---

## 4. TVmaze Integration & Snapshotting

```mermaid
sequenceDiagram
    participant FE as Frontend
    participant API as ShowController/Service
    participant TVMAZE as TVmaze API
    participant DB as PostgreSQL

    FE->>API: GET /api/shows/search?query=...
    API->>TVMAZE: GET /search/shows?q=...
    TVMAZE-->>API: results (not persisted)
    API-->>FE: search results

    FE->>API: POST /api/shows { tvmazeId }
    API->>DB: SELECT show BY tvmaze_id
    alt catalog missing
        API->>TVMAZE: GET /shows/{id} + /shows/{id}/episodes
        TVMAZE-->>API: full show/episode metadata
        API->>DB: INSERT show, seasons, episodes (global catalog)
    end
    API->>DB: INSERT user_library (link user to show)
    API-->>FE: show detail (catalog + library fields)
```

- Search results are never persisted — only an explicit "add to library" creates catalog rows (if missing) and a `user_library` link.
- The catalog is a one-time copy per TVmaze show; TVmaze is not polled afterward for changes (per PRD assumption #2).
- When the last user removes a show, orphan catalog rows are deleted.
- `TvmazeClient` is the only component allowed to call out to TVmaze; it sets a descriptive User-Agent and handles rate-limit backoff/retry.

---

## 5. Watch Cascade Logic

Owned by `WatchService` (downward cascade) and `WatchHierarchySyncService` (upward cascade). Both operations run inside a single DB transaction on `WatchService.markWatched` / `unmarkWatched`.

**Mark watched (episode/season/show):**
1. Upsert `user_watch_state` with `watched = true`, `watched_at = now()` for the target (and descendants for season/show).
2. If target is a season or show, apply the same to all unwatched children in `user_watch_state`, using the *same* timestamp. Already-watched descendants keep their existing timestamp.
3. Write one `watch_events` row per directly affected target (`action = WATCHED`), marking descendant rows as `triggered_by_cascade = true` with `cascade_source_id` pointing to the top-level event.
4. **`WatchHierarchySyncService` (upward):** For each season where every episode is watched, upsert the season row with `watched_at = max(episode.watched_at)`. If every episode in the show is watched, upsert the show row the same way. Write cascade-tagged `watch_events` for each newly promoted parent.
5. **`LibraryStatusSyncService`:** If all episodes are watched, set `user_library.library_status = WATCHED`; otherwise revert from `WATCHED` to `NONE`.

**Unmark watched (episode/season/show):**
1. Frontend must send an explicit confirmation flag for season/show-level unmark requests (backend rejects without it — see 8.1).
2. Set `watched = false` in `user_watch_state` for the target and, for season/show, all descendants.
3. Write corresponding `watch_events` rows (`action = UNWATCHED`) for every directly affected row, same cascade-tagging rule as above.
4. **`WatchHierarchySyncService` (upward):** Clear season/show rows that are watched but no longer have every episode watched. Write cascade-tagged `watch_events` for each demoted parent.
5. **`LibraryStatusSyncService`:** Revert library status if the show is no longer fully watched.

```mermaid
sequenceDiagram
    participant FE as Frontend
    participant WS as WatchService
    participant HS as WatchHierarchySyncService
    participant DB as PostgreSQL

    FE->>WS: POST /api/watch/episodes/{id}
    WS->>DB: BEGIN TRANSACTION
    WS->>DB: UPSERT episode user_watch_state (watched=true)
    WS->>DB: INSERT watch_events (episode, cascade=false)
    WS->>HS: sync upward if season/show complete
    HS->>DB: UPSERT season/show user_watch_state (max episode timestamp)
    HS->>DB: INSERT watch_events (parents, cascade=true)
    WS->>DB: sync library status
    WS->>DB: COMMIT
    WS-->>FE: 204 No Content
```

The entire cascade is one atomic transaction (per PRD §8.3) — a partial cascade is never left in the database.

---

## 6. Analytics Computation

`AnalyticsService` runs read-only aggregate queries against `watch_events` (and joins to `reviews`/`favorites` where relevant). No separate analytics tables/materialized views for MVP — computed on read, since data volume per user is small.

Representative queries:
- **Watch counts by period:** `COUNT(*) FROM watch_events WHERE user_id = ? AND action = 'WATCHED' AND occurred_at BETWEEN ? AND ? GROUP BY target_type`
- **Longest time to watch a show:** for each show, `MAX(occurred_at) - MIN(occurred_at)` over `watch_events` where `target_type = 'EPISODE' AND action = 'WATCHED'`, grouped by the episode's parent show.
- **Favorites (stored):** rows in `favorites` — user-chosen shows only.
- **Favorite suggestions (computed):** weighted scores from user's SHOW/SEASON reviews (season review contributes `rating / season_count`; show review at full weight); per-show max contribution; global max ties; excludes already-favorited shows.

---

## 7. Frontend Architecture

### 7.1 Folder Structure

```
frontend/
└── src/
    ├── api/                  # typed API client functions, one file per backend feature
    │   ├── authApi.ts
    │   ├── showApi.ts
    │   ├── watchApi.ts
    │   ├── reviewApi.ts
    │   ├── favoriteApi.ts
    │   └── analyticsApi.ts
    │
    ├── features/              # feature-oriented UI, mirrors backend features
    │   ├── auth/               (pages, hooks/, authKeys.ts, clearSession.ts)
    │   │   └── hooks/           (useQuery/useMutation per authApi function)
    │   ├── library/             (search, add show, library list/detail)
    │   │   └── hooks/
    │   ├── watch/               (mark/unmark controls, confirmation modal)
    │   │   └── hooks/
    │   ├── reviews/             (review form, review list)
    │   │   └── hooks/
    │   ├── favorites/
    │   │   └── hooks/
    │   └── analytics/           (dashboards/charts)
    │       └── hooks/
    │
    ├── hooks/                  # shared facades only (useAuth composes auth TanStack hooks)
    ├── components/              # shared/dumb UI components (buttons, stars, modals)
    ├── routes/                  # route definitions
    └── lib/
        └── queryClient.ts        # TanStack Query client config
```

### 7.2 Server State — TanStack Query
- Every `api/*.ts` function is wrapped in a `useQuery`/`useMutation` hook within its feature folder (e.g. `useShowLibrary()`, `useMarkWatched()`).
- Query keys are namespaced by feature and target, e.g. `['shows', userId]`, `['watch-status', targetType, targetId]`, `['analytics', 'watch-counts', period]`.
- Mutations (mark/unmark watched, add review, toggle favorite) invalidate the relevant query keys on success — e.g. marking a show watched invalidates that show's detail query *and* all descendant season/episode queries *and* the analytics queries, since cascades affect all of them.
- Local component/UI-only state (form inputs, modal open/closed) uses plain `useState`, not TanStack Query.

### 7.2.1 Auth and session

- JWT in `localStorage` via `api/client.ts` is client session plumbing (not TanStack-managed).
- `/api/me` and all other API responses use TanStack Query only.
- `hooks/useAuth` is a shared **facade** composing TanStack hooks; it must not fetch directly.
- Reference implementation: `features/auth/hooks/`, `authKeys.ts`, `clearSession.ts`.

**Deprecated patterns (superseded by `frontend-tanstack-auth`; do not reintroduce):**

| Dead pattern | Replacement |
|---|---|
| `AuthProvider` / React Context for user state | `QueryClientProvider` + `useCurrentUser()` |
| `refreshUser()` | `queryClient.invalidateQueries({ queryKey: authKeys.me() })` |
| `useState`/`useEffect` + `authApi.getCurrentUser()` | `useCurrentUser()` |
| Direct `authApi.*` in pages | `features/auth/hooks/use*.ts` |
| `getErrorMessage` exported from `useAuth.tsx` | `lib/getErrorMessage.ts` |

### 7.3 Cascade Confirmation UX
Unmarking a season/show as watched triggers a confirmation modal (shared `useConfirm` hook) before the mutation fires — matching the backend's requirement for an explicit confirm flag (Section 5).

---

## 8. Cross-Cutting Concerns

### 8.1 Security
- JWT issued on login, sent as `Authorization: Bearer <token>` on every request.
- `CurrentUserResolver` injects the authenticated user's ID into every service call — repositories always filter by this ID; no endpoint accepts a client-supplied user ID.
- Season/show-level unmark endpoints require a `confirm=true` request parameter; requests without it return `400` with a message directing the client to confirm.

### 8.2 Error Handling
- `GlobalExceptionHandler` (`@ControllerAdvice`) maps domain exceptions (`NotFoundException`, `ValidationException`, `ForbiddenException`) to consistent JSON error responses with appropriate HTTP status codes.

### 8.3 Testing Strategy
- **Backend:** JUnit 5 + Mockito for service-layer unit tests (especially `WatchService` cascade logic and `AnalyticsService` calculations); `@SpringBootTest` + Testcontainers (Postgres) for controller/repository integration tests.
- **Frontend:** Vitest + React Testing Library for component/hook unit tests; integration tests for key flows (search → add show, mark watched → cascade UI update, write review) using mocked API responses.
- Each feature package/folder (backend and frontend alike) owns its own tests colocated alongside the code they test.

### 8.4 Local Development Setup
- `docker-compose.yml` at the repo root spins up PostgreSQL for local development only (app processes themselves run natively via Gradle/pnpm, not containerized yet — per PRD §7.4).
- Backend configuration via Spring profiles (`application-local.yml`) for DB connection, optional TVmaze base URL, and JWT secret.
- Frontend configuration via `.env.local` for the API base URL.

---

## 9. Open Items Carried Over from PRD

The three assumptions flagged in `PRD.md` §9 directly affect this architecture (schema for `library_status`, the snapshot-vs-live-sync model in Section 4, and cascade-delete behavior) — if those change, the schema and TVmaze integration section will need revisiting accordingly.