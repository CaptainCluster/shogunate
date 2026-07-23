## Why

Watch tracking, reviews, and favorites are complete, but users still cannot view statistics derived from their watch history — the core Phase 6 capability from the PRD. This backend change delivers read-only analytics REST endpoints (including watch streaks, library completion, and plan-to-watch count) so a follow-up frontend change can build the dashboard without further backend work.

## What Changes

- Create `analytics/` package: `AnalyticsService`, `AnalyticsController`, DTOs
- REST under `/api/analytics` (seven GET endpoints):
  - `GET /watch-counts?period=&from=&to=` — WATCHED event counts by target type in a resolved date range
  - `GET /longest-to-watch` — per-show elapsed time between first and last watched episode, ranked descending
  - `GET /totals` — all-time WATCHED event counts by target type
  - `GET /favorites` — thin wrapper over Phase 5 `FavoriteService.listFavorites`
  - `GET /watch-streaks` — current and longest consecutive-day watch streaks from event log
  - `GET /library-completion` — per-show and overall episode completion % from current watch state
  - `GET /plan-to-watch-count` — count of library shows flagged `PLAN_TO_WATCH`
- Extend `WatchEventRepository` and `UserLibraryRepository` with aggregate queries (no new tables or Flyway migration)
- Unit + integration tests covering all metrics, period resolution, edge cases, and cross-user isolation
- Complete Phase 6 backend tasks **6.1**, **6.2**, **6.3**, **6.5**; check off `openspec/TASKS.md` on archive

## Capabilities

### New Capabilities

None — behavioral requirements live in or extend `openspec/specs/analytics/spec.md` via delta spec.

### Modified Capabilities

- `analytics`: ADDED requirements for total watched counts, watch streaks, library completion, and plan-to-watch count

Watch-counts period resolution (`MONTH`/`YEAR` derive `to` from `from`; `CUSTOM` requires explicit `to`) is an API design detail captured in `design.md`.

## Impact

- **Backend:** new `com.tvtracker.analytics/` package; custom queries on `WatchEventRepository` and `UserLibraryRepository`
- **API:** seven GET endpoints under `/api/analytics`
- **Database:** read-only against existing `watch_events`, `user_watch_state`, `user_library`, `favorites`, and catalog tables
- **Tests:** `AnalyticsServiceTest`, `AnalyticsIntegrationTest`; update `OpenApiIntegrationTest`
- **Docs:** check off Phase 6 tasks in `openspec/TASKS.md` on archive

## Non-goals

- Frontend analytics dashboard (Phase 6.4 — separate `analytics-frontend` change)
- New DB tables, materialized views, or server-side caching
- Enriching `/api/analytics/favorites` with show titles beyond existing `FavoriteResponse`
- Streak history calendar / heatmap UI
- Plan-to-watch show list (count only; frontend can filter library)
- Season/episode-level plan-to-watch (show-level only per product assumption)
