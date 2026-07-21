## Why

Show library and shared catalog are in place, but users cannot yet mark episodes, seasons, or shows as watched — the core product loop from the PRD. Phase 3 backend delivers watch state, immutable history for future analytics, and REST APIs so a separate frontend change can wire UI controls without further backend work.

## What Changes

- Add Flyway **V5**: append-only `watch_events` table (V4 catalog migration already exists as `V4__shared_show_catalog.sql`)
- Create `watch/` package: `WatchService` (cascade mark/unmark), `WatchController`, `WatchEvent` entity/repository
- Move `UserWatchState` + repository from `show/` → `watch/`
- REST: `POST`/`DELETE` on `/api/watch/episodes|seasons|shows/{id}`; season/show unmark requires `confirm=true`
- Enrich `GET /api/shows/{id}` with `watched` / `watchedAt` on show, season, and episode
- Wire `ShowService.removeFromLibrary` to delete user's `watch_events` for the show hierarchy
- Add JaCoCo with **≥ 80% line coverage** gate in `./gradlew check` (local; no SonarQube server)
- Update `openspec/TASKS.md` Phase 3 notes (backend vs deferred frontend)

## Capabilities

### New Capabilities

None — behavioral requirements live in existing `watch` and `show` specs; this change implements them.

### Modified Capabilities

- `watch`: REST endpoints, library-membership scoping, cascade event tagging (`triggered_by_cascade`, `cascade_source_id`), confirm flag on season/show unmark
- `show`: show detail includes watch state; remove-from-library deletes `watch_events`

## Impact

- **Backend:** new `com.tvtracker.watch/` package; migrations V4 (if missing) + V5; `ShowService` / show DTOs; JaCoCo in `build.gradle.kts`
- **API:** six new watch mutation endpoints; extended show detail response shape
- **Database:** `watch_events` table; possible V4 catalog tables if not yet migrated
- **Tests:** `WatchServiceTest`, `WatchIntegrationTest`; expanded show tests; coverage verification task
- **Docs:** `openspec/TASKS.md` Phase 3 clarifications

## Non-goals

- Frontend watch controls, confirmation modal, TanStack invalidation (Phase 3.5 — separate change)
- Analytics queries over `watch_events` (Phase 6)
- Reviews and favorites (Phases 4–5)
- Dedicated watch status GET endpoints (read via enriched show detail)
- SonarQube / SonarCloud integration
