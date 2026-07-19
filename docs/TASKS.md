# Implementation Tasks: TV Show Tracker

Companion to `PRD.md` and `ARCHITECTURE.md`. Tasks are grouped into phases, ordered by dependency. Each task lists its acceptance criteria and the PRD/Architecture section(s) it implements, so completion can be verified against the spec rather than by inspection alone.

Checkboxes are for tracking progress as work is completed.

---

## Phase 0: Foundation & Setup

- [ ] **0.1 — Repo scaffolding**
  Create `/backend` (Gradle, Spring Boot init) and `/frontend` (pnpm, React + TypeScript, Vite) as independent projects at the repo root.
  *Acceptance:* `./gradlew build` succeeds in `/backend`; `pnpm install && pnpm build` succeeds in `/frontend`. Neither depends on the other to build.
  *Ref: Architecture §7.2 (monorepo structure)*

- [ ] **0.2 — Local Postgres via Docker Compose**
  Add root-level `docker-compose.yml` running Postgres for local dev only.
  *Acceptance:* `docker-compose up` starts a reachable Postgres instance; backend connects to it via `application-local.yml`.
  *Ref: Architecture §8.4*

- [ ] **0.3 — Backend base config**
  Set up Spring profiles, `common/exception` (`GlobalExceptionHandler`), and `config/OpenApiConfig` (springdoc-openapi).
  *Acceptance:* `/swagger-ui.html` loads with an empty API; a thrown `ApiException` returns a structured JSON error, not a raw stack trace.
  *Ref: Architecture §2.2, §8.2*

- [ ] **0.4 — Frontend base setup**
  Set up routing (`routes/`), TanStack Query client (`lib/queryClient.ts`), and shared `components/` shell (layout, nav).
  *Acceptance:* App boots to an empty shell with routing and a configured QueryClientProvider.
  *Ref: Architecture §7.1, §7.2*

- [ ] **0.5 — Database migrations tool**
  Introduce a migration tool (e.g. Flyway) and write the initial migration creating `users` (only — other tables added per-feature below, in their own phases, so schema changes stay tied to the feature that needs them).
  *Acceptance:* Migrations run cleanly on a fresh DB via `./gradlew flywayMigrate` or on app startup.

---

## Phase 1: Authentication

- [ ] **1.1 — `users`, email verification & password reset tables (migration)**
  *Ref: Architecture §3 (users, and the two token tables)*

- [ ] **1.2 — Register + login (backend)**
  `AuthController`, `AuthService`, `UserRepository`. Password hashing (bcrypt/Argon2). JWT issuance on login.
  *Acceptance:* `POST /api/auth/register` creates an unverified user; `POST /api/auth/login` fails for unverified users; succeeds and returns a JWT for verified ones.
  *Ref: PRD §5.1; Architecture §2.1, §8.1*

- [ ] **1.3 — Email verification flow (backend)**
  Token generation + `POST /api/auth/verify-email`, `POST /api/auth/resend-verification`. (Actual email sending can be stubbed/logged for local dev.)
  *Acceptance:* A newly registered user cannot log in until their verification token is redeemed.
  *Ref: PRD §5.1*

- [ ] **1.4 — Password reset flow (backend)**
  `POST /api/auth/forgot-password`, `POST /api/auth/reset-password`.
  *Acceptance:* Requesting reset issues a time-limited token; redeeming it updates the password hash and invalidates the token.
  *Ref: PRD §5.1*

- [ ] **1.5 — JWT auth filter + `CurrentUserResolver` (backend)**
  Every authenticated endpoint resolves the current user from the token; no endpoint accepts a client-supplied user ID.
  *Acceptance:* Requests without a valid token return `401`; a manually-crafted request with someone else's ID in the body/path still only ever operates on the token's user.
  *Ref: Architecture §8.1*

- [ ] **1.6 — Auth UI (frontend)**
  Register, login, verify-email, forgot/reset-password pages + `authApi.ts` + `useAuth` hook.
  *Acceptance:* Full signup → verify → login → reset-password flow works end-to-end against the local backend.
  *Ref: Architecture §7.1*

- [ ] **1.7 — Tests**
  Unit tests for `AuthService` (hashing, token expiry logic). Integration tests for all `/api/auth/*` endpoints.
  *Ref: Architecture §8.3*

---

## Phase 2: Show Discovery & Library

- [ ] **2.1 — `shows`, `seasons`, `episodes` tables (migration)**
  *Ref: Architecture §3*

- [ ] **2.2 — TMDb client (backend)**
  `TmdbClient` + `TmdbMapper` + `config/TmdbConfig` (API key, base URL, backoff/retry).
  *Acceptance:* Client successfully calls TMDb search and detail endpoints against a real (or sandboxed) TMDb API key.
  *Ref: Architecture §4*

- [ ] **2.3 — Show search (backend)**
  `GET /api/shows/search?query=` — proxies TMDb, does not persist results.
  *Acceptance:* Returns TMDb search results; nothing is written to the DB from a search alone.
  *Ref: PRD §5.2; Architecture §4*

- [ ] **2.4 — Add show to library / snapshot (backend)**
  `POST /api/shows { tmdbId }` — fetches full show/season/episode metadata from TMDb and writes the local snapshot.
  *Acceptance:* Adding a show creates rows in `shows`, `seasons`, and `episodes` scoped to the requesting user; adding the same TMDb show twice for the same user is rejected or is a no-op (decide and document at implementation time).
  *Ref: PRD §5.2; Architecture §4*

- [ ] **2.5 — Library CRUD (backend)**
  `GET /api/shows`, `GET /api/shows/{id}` (with seasons/episodes), `PATCH /api/shows/{id}` (set `library_status`), `DELETE /api/shows/{id}`.
  *Acceptance:* All endpoints are scoped to the authenticated user; deleting a show removes its seasons/episodes and any dependent reviews/watch-events/favorites (cascade delete or explicit cleanup — confirm approach at implementation time, see PRD §9 open assumption #3).
  *Ref: PRD §5.2*

- [ ] **2.6 — Library UI (frontend)**
  Search page, "add to library" action, library list (with status filter), show detail page (seasons/episodes list).
  *Ref: Architecture §7.1*

- [ ] **2.7 — Tests**
  Unit tests for `TmdbMapper` and snapshot logic. Integration tests for search/add/list/detail/delete endpoints (TMDb calls mocked).
  *Ref: Architecture §8.3*

---

## Phase 3: Watch Tracking

- [ ] **3.1 — `watch_events` table (migration)**
  Append-only; no update/delete exposed at the repository layer.
  *Ref: Architecture §3*

- [ ] **3.2 — Mark-watched cascade (backend)**
  `WatchService`: marking an episode/season/show watched, cascading down to children with a shared timestamp, writing one `watch_events` row per affected row, all in a single transaction.
  *Acceptance:* Marking a show watched results in every episode/season under it being watched with the same `watched_at`, and one `watch_events` row per affected episode/season/show, correctly tagged with `triggered_by_cascade`.
  *Ref: PRD §5.3; Architecture §5*

- [ ] **3.3 — Unmark-watched cascade with confirmation (backend)**
  Season/show-level unmark requires `confirm=true`; missing it returns `400`. Cascades down to children, same transactional/logging behavior as marking.
  *Acceptance:* Unmarking a show without `confirm=true` is rejected; with it, the show and all its seasons/episodes become unwatched, each logged.
  *Ref: PRD §5.3; Architecture §5, §8.1*

- [ ] **3.4 — Watch endpoints (backend)**
  `POST`/`DELETE` under `/api/watch/episodes/{id}`, `/api/watch/seasons/{id}`, `/api/watch/shows/{id}`.
  *Ref: Architecture §2.1 (package layout)*

- [ ] **3.5 — Watch controls + confirmation modal (frontend)**
  Mark/unmark buttons at episode/season/show level; `useConfirm` modal wired to season/show unmark actions; TanStack Query cache invalidation covering the target and all its ancestors/descendants.
  *Acceptance:* Unmarking a season in the UI prompts for confirmation before firing the request; after a cascade action, all affected rows update in the UI without a manual refresh.
  *Ref: PRD §5.3; Architecture §7.2, §7.3*

- [ ] **3.6 — Tests**
  Unit tests for cascade logic (mark and unmark, including partial-failure rollback). Integration tests for the confirm-flag requirement.
  *Ref: Architecture §8.3*

---

## Phase 4: Reviews & Ratings

- [ ] **4.1 — `reviews` table (migration)**
  Unique on `(user_id, target_type, target_id)`.
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

- [ ] **5.1 — `favorites` table (migration)**
  Unique on `(user_id, target_type, target_id)`; `target_type` restricted to `SHOW`/`SEASON` at the application layer.
  *Ref: Architecture §3, and the earlier discussion of the `target_type` discriminator pattern*

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
  Verify no query anywhere can leak one user's shows/reviews/watch-events/favorites to another user, across every feature built above.
  *Ref: PRD §2, §4 (no cross-user visibility)*

- [ ] **7.3 — OpenAPI review**
  Confirm `/swagger-ui.html` accurately reflects every implemented endpoint, with meaningful operation summaries/descriptions.
  *Ref: Architecture §2.2*

- [ ] **7.4 — End-to-end smoke pass**
  Manually walk the full flow: register → verify → add show → mark episodes/season/show watched (and unmark with confirmation) → write reviews → flag favorite → view analytics.
  *Ref: PRD §5 (all functional requirements)*

---

## Notes on Sequencing

- Phases are ordered by dependency: Auth must exist before anything user-scoped; Show/library must exist before Watch, Reviews, or Favorites can attach to anything; Analytics depends on Watch, Reviews, and Favorites all being in place.
- Within each phase, backend tasks precede frontend tasks, since the frontend integrates against a working API.
- The three open assumptions from `PRD.md` §9 surface concretely in tasks **2.4** (duplicate-add behavior), **2.5** (delete cascade behavior), and Architecture §4 (snapshot vs. live-sync) — resolve those before or during Phase 2 rather than deferring them.