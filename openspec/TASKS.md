# Implementation Tasks: TV Show Tracker

> **Status: live roadmap.** Canonical phase-level checklist for the project.
> Per-feature implementation steps live in `openspec/changes/<change-name>/tasks.md` (created with `/opsx:propose`, completed via `/opsx:apply`).
> Behavior is defined in `openspec/specs/`; package and DB detail in `docs/ARCHITECTURE.md`.

Companion to `docs/PRD.md` and `docs/ARCHITECTURE.md`. Tasks are grouped into phases, ordered by dependency. Each task lists its acceptance criteria and the PRD/Architecture section(s) it implements, so completion can be verified against the spec rather than by inspection alone.

Checkboxes track phase-level progress. When an OpenSpec change completes and is archived, check off the corresponding phase task(s) here in the same commit.

**Completed changes (archived):** `foundation-setup`, `frontend-tanstack-auth`, `username-auth`, `tvmaze-api`, `shared-show-catalog`.

---



## Phase 0: Foundation & Setup

- [x] **0.1 — Repo scaffolding**
  Create `/backend` (Gradle, Spring Boot init) and `/frontend` (pnpm, React + TypeScript, Vite) as independent projects at the repo root.
  *Acceptance:* `./gradlew build` succeeds in `/backend`; `pnpm install && pnpm build` succeeds in `/frontend`. Neither depends on the other to build.
  *Ref: Architecture §7.2 (monorepo structure)*

- [x] **0.2 — Local Postgres via Docker Compose**
  Add root-level `docker-compose.yml` running Postgres for local dev only.
  *Acceptance:* `docker-compose up` starts a reachable Postgres instance; backend connects to it via `application-local.yml`.
  *Ref: Architecture §8.4*

- [x] **0.3 — Backend base config**
  Set up Spring profiles, `common/exception` (`GlobalExceptionHandler`), and `config/OpenApiConfig` (springdoc-openapi).
  *Acceptance:* `/swagger-ui.html` loads with an empty API; a thrown `ApiException` returns a structured JSON error, not a raw stack trace.
  *Ref: Architecture §2.2, §8.2*

- [x] **0.4 — Frontend base setup**
  Set up routing (`routes/`), TanStack Query client (`lib/queryClient.ts`), and shared `components/` shell (layout, nav).
  *Acceptance:* App boots to an empty shell with routing and a configured QueryClientProvider.
  *Ref: Architecture §7.1, §7.2*

- [x] **0.5 — Database migrations tool**
  Introduce a migration tool (e.g. Flyway) and write the initial migration creating `users` (only — other tables added per-feature below, in their own phases, so schema changes stay tied to the feature that needs them).
  *Acceptance:* Migrations run cleanly on a fresh DB via `./gradlew flywayMigrate` or on app startup.

---



## Phase 1: Authentication

> Revised by the `username-auth` OpenSpec change: auth uses username + password only (no email verification or password reset). Frontend auth uses TanStack Query hooks (`frontend-tanstack-auth`).

- [x] **1.1 —** `users` **table (migration)**
  Flyway migration for `users` with `username` (unique, case-insensitive lookup).
  *Ref: Architecture §3*

- [x] **1.2 — Register + login (backend)**
  `AuthController`, `AuthService`, `UserRepository`. Password hashing (bcrypt). JWT issuance on login.
  *Acceptance:* `POST /api/auth/register` creates a user; `POST /api/auth/login` returns a JWT for valid credentials.
  *Ref: PRD §5.1; Architecture §2.1, §8.1*

- [x] **1.3 — JWT auth filter +** `CurrentUserResolver` **(backend)**
  Every authenticated endpoint resolves the current user from the token; no endpoint accepts a client-supplied user ID.
  *Acceptance:* Requests without a valid token return `401`; a manually-crafted request with someone else's ID in the body/path still only ever operates on the token's user.
  *Ref: Architecture §8.1*

- [x] **1.4 — Auth UI (frontend)**
  Register and login pages + `authApi.ts` + `useAuth` hook.
  *Acceptance:* Register → login flow works end-to-end against the local backend.
  *Ref: Architecture §7.1*

- [x] **1.5 — Tests**
  Unit tests for `AuthService`. Integration tests for register/login and `/api/me`.
  *Ref: Architecture §8.3*

---



## Phase 2: Show Discovery & Library

> Revised by `shared-show-catalog` and `tvmaze-api`: global shared catalog keyed by `tvmaze_id`; per-user `user_library` membership; TVmaze-only backend integration; one-time catalog snapshot (no live sync after first add).

- [x] **2.1 — Shared catalog + library tables (migration)**
  Flyway V4: global `shows`, `seasons`, `episodes` (no `user_id`; unique `tvmaze_id` on shows); `user_library` with `library_status` and `added_at`; `user_watch_state` (prep for Phase 3).
  *Acceptance:* Migration runs cleanly on a fresh DB; catalog tables have no user-scoping columns.
  *Ref: Architecture §3*

- [x] **2.2 — TVmaze client (backend)**
  `TvmazeClient` + `TvmazeMapper` + `config/TvmazeConfig` (base URL, User-Agent, backoff/retry).
  *Acceptance:* Client successfully calls TVmaze search and snapshot endpoints against the public API.
  *Ref: Architecture §4*

- [x] **2.3 — Show search (backend)**
  `GET /api/shows/search?query=` — proxies TVmaze, does not persist results.
  *Acceptance:* Returns TVmaze search results; nothing is written to the DB from a search alone.
  *Ref: PRD §5.2; Architecture §4*

- [x] **2.4 — Add show to library (backend)**
  `POST /api/shows { tvmazeId }` — if catalog missing, fetches full show/episode metadata from TVmaze and creates global catalog rows; always inserts `user_library` link for the requesting user.
  *Acceptance:* First add creates global `shows`/`seasons`/`episodes` plus a `user_library` row; adding the same TVmaze show again for the same user returns 409 Conflict; a second user adding the same show reuses the existing catalog without a TVmaze call.
  *Ref: PRD §5.2; Architecture §4*

- [x] **2.5 — Library CRUD (backend)**
  `GET /api/shows`, `GET /api/shows/{id}` (with seasons/episodes), `PATCH /api/shows/{id}` (set `library_status`), `DELETE /api/shows/{id}`.
  *Acceptance:* List and detail require library membership for the authenticated user; remove deletes the user's `user_library` row and user-scoped data for that show hierarchy; global catalog is orphan-deleted when no users remain linked; catalog is preserved when other users still have the show in their library.
  *Ref: PRD §5.2*

- [x] **2.6 — Library UI (frontend)**
  Search page, "add to library" action, library list, show detail page (seasons/episodes list), `/about` page with TVmaze CC BY-SA attribution. TanStack Query hooks in `features/library/`.
  *Ref: Architecture §7.1*

- [x] **2.7 — Tests**
  Unit tests for `TvmazeMapper` and duplicate-add rejection. Integration tests for search/add/list/detail/delete with mocked TVmaze, including catalog reuse by a second user and orphan delete when the last user removes a show.
  *Ref: Architecture §8.3*

---



## Phase 3: Watch Tracking

> `user_watch_state` table and entity already exist (Phase 2 / V4). Phase 3 adds immutable `watch_events` history and `WatchService` cascade logic that updates both `user_watch_state` and `watch_events`. Targets reference shared catalog IDs scoped by `user_id`.
>
> **Backend change:** `watch-tracking-backend` implements 3.1–3.4 and the backend portion of 3.6. **Frontend (3.5)** implemented in `watch-tracking-frontend`.

- [x] **3.1 —** `watch_events` **table (migration)**
  Append-only history log during normal watch/unwatch; bulk delete permitted only on library removal.
  *Ref: Architecture §3*

- [x] **3.2 — Mark-watched cascade (backend)**
  `WatchService`: upsert `user_watch_state` for the target (and descendants for season/show) with a shared timestamp; write one `watch_events` row per affected row, all in a single transaction.
  *Acceptance:* Marking a show watched results in every episode/season under it being watched with the same `watched_at`, and one `watch_events` row per affected episode/season/show, correctly tagged with `triggered_by_cascade`.
  *Ref: PRD §5.3; Architecture §5*

- [x] **3.3 — Unmark-watched cascade with confirmation (backend)**
  Season/show-level unmark requires `confirm=true`; missing it returns `400`. Cascades down to children, updating `user_watch_state` and logging to `watch_events` in one transaction.
  *Acceptance:* Unmarking a show without `confirm=true` is rejected; with it, the show and all its seasons/episodes become unwatched, each logged.
  *Ref: PRD §5.3; Architecture §5, §8.1*

- [x] **3.4 — Watch endpoints (backend)**
  `POST`/`DELETE` under `/api/watch/episodes/{id}`, `/api/watch/seasons/{id}`, `/api/watch/shows/{id}`.
  *Ref: Architecture §2.1 (package layout)*

- [x] **3.4b — Show detail watch state (backend)**
  `GET /api/shows/{id}` includes `watched` and `watchedAt` on show, season, and episode for the authenticated user.
  *Acceptance:* Unwatched targets default to `watched=false`; after mark operations, detail reflects current state without separate watch GET endpoints.
  *Ref: Architecture §2.1; prepares Phase 3.5 frontend*

- [x] **3.5 — Watch controls + confirmation modal (frontend)**
  Mark/unmark buttons at episode/season/show level; `useConfirm` modal wired to season/show unmark actions; TanStack Query cache invalidation covering the target and all its ancestors/descendants.
  *Acceptance:* Unmarking a season in the UI prompts for confirmation before firing the request; after a cascade action, all affected rows update in the UI without a manual refresh.
  *Ref: PRD §5.3; Architecture §7.2, §7.3*

- [x] **3.6 — Tests (backend)**
  Unit tests for cascade logic (mark and unmark, including partial-failure rollback). Integration tests for the confirm-flag requirement and cross-user isolation. JaCoCo ≥ 80% line coverage enforced in `./gradlew check` (local gate; no SonarQube server).
  *Ref: Architecture §8.3*

---



## Phase 4: Reviews & Ratings

- [ ] **4.1 —** `reviews` **table (migration)**
  Unique on `(user_id, target_type, target_id)`; targets reference shared catalog entities.
  *Ref: Architecture §3*

- [ ] **4.2 — Review CRUD (backend)**
  `POST`/`PUT`/`DELETE /api/reviews`, `GET /api/reviews?targetType=&targetId=`. Rating validated to 0.5-step increments in `[0.5, 5.0]`.
  *Acceptance:* Reviews at episode/season/show level are stored and retrieved independently, with no roll-up logic between levels. A user cannot fetch or modify another user's review.
  *Ref: PRD §5.4*

- [ ] **4.3 — Review UI (frontend)**
  Star-rating input (half-star precision) + text field, attachable to episode/season/show detail views.
  *Ref: PRD §5.4; Architecture §7.1*

- [ ] **4.4 — Tests**
  Unit tests for rating validation. Integration tests for CRUD + cross-user access rejection.
  *Ref: Architecture §8.3*

---



## Phase 5: Favorites

- [ ] **5.1 —** `favorites` **table (migration)**
  Unique on `(user_id, target_type, target_id)`; `target_type` restricted to `SHOW`/`SEASON` at the application layer.
  *Ref: Architecture §3, and the earlier discussion of the* `target_type` *discriminator pattern*

- [ ] **5.2 — Favorite auto-suggestion + manual override (backend)**
  `AnalyticsService` (or `FavoriteService`, reading from reviews) computes auto-suggested favorites by average rating; `FavoriteController` exposes manual flag/unflag via `POST`/`DELETE /api/favorites`.
  *Acceptance:* `GET` for favorites returns both manually-flagged items and auto-suggested ones, distinguishable via `is_manual`.
  *Ref: PRD §5.5*

- [ ] **5.3 — Favorites UI (frontend)**
  Favorite toggle on show/season views; visual distinction between auto-suggested and manually-flagged.
  *Ref: PRD §5.5*

- [ ] **5.4 — Tests**
  Unit tests for the auto-suggestion calculation. Integration tests for manual flag/unflag.
  *Ref: Architecture §8.3*

---



## Phase 6: Analytics

- [ ] **6.1 — Watch-counts-by-period query (backend)**
  `GET /api/analytics/watch-counts?period=&from=&to=`, grouped by target type, sourced from `watch_events`.
  *Ref: PRD §5.6; Architecture §6*

- [ ] **6.2 — Longest-time-to-watch query (backend)**
  `GET /api/analytics/longest-to-watch` — per-show `MAX(occurred_at) - MIN(occurred_at)` over watched episode events, ranked descending.
  *Ref: PRD §5.6; Architecture §6*

- [ ] **6.3 — Totals + favorites endpoints (backend)**
  `GET /api/analytics/totals`, `GET /api/analytics/favorites` (thin wrapper over Phase 5 logic for dashboard consumption).
  *Ref: PRD §5.6*

- [ ] **6.4 — Analytics dashboard (frontend)**
  Charts/lists for favorites, watch counts by period, longest-to-watch ranking, totals.
  *Ref: PRD §5.6; Architecture §7.1*

- [ ] **6.5 — Tests**
  Unit tests for each analytics calculation, using seeded `watch_events` fixtures covering edge cases (single-episode shows, shows with only cascade-triggered events, empty history).
  *Ref: Architecture §8.3*

---



## Phase 7: Cross-Cutting Polish

- [ ] **7.1 — Security review**
  Confirm every endpoint scopes queries by the authenticated user (no ID accepted from client input is trusted without an ownership check); confirm password hashes and JWT secret are never logged.
  *Ref: Architecture §8.1*

- [ ] **7.2 — Data-isolation audit**
  Verify no query anywhere can leak one user's library entries, reviews, watch-events, or favorites to another user, across every feature built above.
  *Ref: PRD §2, §4 (no cross-user visibility)*

- [ ] **7.3 — OpenAPI review**
  Confirm `/swagger-ui.html` accurately reflects every implemented endpoint, with meaningful operation summaries/descriptions.
  *Ref: Architecture §2.2*

- [ ] **7.4 — End-to-end smoke pass**
  Manually walk the full flow: register → login → add show → mark episodes/season/show watched (and unmark with confirmation) → write reviews → flag favorite → view analytics.
  *Ref: PRD §5 (all functional requirements)*

---



## Notes on Sequencing

- Phases are ordered by dependency: Auth must exist before anything user-scoped; Show/library must exist before Watch, Reviews, or Favorites can attach to anything; Analytics depends on Watch, Reviews, and Favorites all being in place.
- Within each phase, backend tasks precede frontend tasks, since the frontend integrates against a working API.



### Resolved decisions (formerly open in PRD §9 / Architecture §4)

- **Duplicate add:** Return 409 when `(user_id, show_id)` already exists in `user_library`.
- **Remove from library:** Delete user-scoped data and the `user_library` row; orphan-delete global catalog when no users remain; preserve catalog for other users.
- **Metadata freshness:** One-time TVmaze snapshot at first catalog create; no live sync afterward.



### Remaining open assumption

- **Plan to Watch scope:** Currently implemented at show level only (`user_library.library_status`). Confirm this remains the intended product scope before extending status to seasons/episodes.

