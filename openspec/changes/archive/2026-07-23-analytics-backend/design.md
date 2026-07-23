## Context

Phases 0–5 shipped auth, show library, watch tracking, reviews, and favorites. [`openspec/specs/analytics/spec.md`](../../specs/analytics/spec.md) defines behavioral requirements but no `analytics/` implementation exists yet. Latest Flyway migration is **V9**; this change adds **no migration** — analytics reads existing tables only.

**Constraints:**
- Time-based metrics sourced from `watch_events` where `action = WATCHED`
- Completion metrics sourced from `user_watch_state` (current snapshot, not event log)
- Plan-to-watch count sourced from `user_library.library_status`
- Every query scoped to JWT-resolved `userId` — no client-supplied user ID
- `watch_events` remains append-only during normal operations
- Favorites analytics delegates to Phase 5 `FavoriteService` (explicit favorites only, no suggestions)

## Goals / Non-Goals

**Goals:**
- `analytics/` vertical slice: controller → service → DTOs
- Seven GET endpoints under `/api/analytics`
- Aggregate queries on `WatchEventRepository` and `UserLibraryRepository`
- Complete Phase 6 backend tasks 6.1, 6.2, 6.3, 6.5

**Non-Goals:**
- Frontend dashboard (Phase 6.4 — separate change)
- New DB tables, materialized views, or server-side caching
- Enriching favorites response with show titles
- Streak calendar UI or plan-to-watch list endpoint

## Decisions

### Package layout: new `analytics/`

**Decision:** Create `com.tvtracker.analytics/` with `AnalyticsController`, `AnalyticsService`, and DTOs. Extend `WatchEventRepository` and `UserLibraryRepository` with custom `@Query` methods.

**Rationale:** Matches ARCHITECTURE §2.1 feature-based vertical slices; analytics is read-only and cross-cutting.

### Data source split

| Endpoint | Source | Rationale |
|----------|--------|-----------|
| watch-counts, longest-to-watch, totals, watch-streaks | `watch_events` (WATCHED) | Time-based / activity metrics per spec |
| library-completion | `user_watch_state` + catalog | Snapshot completion % (mirrors `LibraryStatusSyncService`) |
| plan-to-watch-count | `user_library` | Show-level status flag |
| favorites | `FavoriteService` | Phase 5 delegate |

### Watch counts: period resolution

**Decision:** `GET /api/analytics/watch-counts` accepts:

| Param | Required | Notes |
|-------|----------|-------|
| `period` | yes | `MONTH`, `YEAR`, or `CUSTOM` |
| `from` | yes | ISO date `YYYY-MM-DD` — anchor date |
| `to` | CUSTOM only | ISO date; omitted for MONTH/YEAR |

**Resolved ranges:**
- `MONTH`: first → last day of calendar month containing `from` (ignore client `to` if sent)
- `YEAR`: Jan 1 → Dec 31 of calendar year containing `from`
- `CUSTOM`: `from` → `to` inclusive; reject missing `to` or `from > to` with `ValidationException`

Convert resolved dates to `Instant` bounds (start-of-day / end-of-day UTC). Response echoes resolved `from`, `to`, and `period`.

### Counting semantics

**Decision:** Count raw `WATCHED` rows grouped by `target_type`. UNWATCHED events excluded. Cascade-triggered WATCHED events included. Missing target types default to `0`.

### Longest-to-watch query

**Decision:** Native SQL joining `watch_events` → `episodes` → `seasons` → `shows`, inner-join `user_library`. Filter: `user_id`, `target_type = EPISODE`, `action = WATCHED`. Group by show; compute duration; order DESC.

**Response:** `showId`, `title`, `durationSeconds`, `firstWatchedAt`, `lastWatchedAt`.

### Totals and favorites endpoints

- `GET /api/analytics/totals` — same counting rules as watch-counts, no date filter
- `GET /api/analytics/favorites` — delegate to `favoriteService.listFavorites(userId)`

### Watch streaks

**Decision:** `GET /api/analytics/watch-streaks` returns current and longest consecutive-day streaks.

**Streak day:** any UTC calendar day with ≥1 `watch_events` row where `action = WATCHED` (any `target_type`).

**Algorithm** (service layer after `findDistinctWatchedDates(userId)`):
1. Sort distinct activity dates ascending
2. **Longest streak:** max run of consecutive calendar days
3. **Current streak:** walk backward from most recent activity date; if most recent date is neither today nor yesterday (UTC), current streak = 0

**Response:** `currentStreakDays`, `longestStreakDays`, `currentStreakStartDate`, `longestStreakStartDate`, `longestStreakEndDate` (nullable when streak is 0).

### Library completion

**Decision:** `GET /api/analytics/library-completion` returns overall and per-show completion.

**Per show:** `watchedEpisodes / totalEpisodes * 100` where:
- `totalEpisodes` = catalog episode count for show
- `watchedEpisodes` = episodes with `user_watch_state.watched = true` and `target_type = EPISODE`

Same logic as [`LibraryStatusSyncService.isFullyWatched`](../../../backend/src/main/java/com/tvtracker/show/LibraryStatusSyncService.java). Native SQL preferred (join `user_library` → seasons → episodes, left join `user_watch_state`).

**Response:** `overallCompletionPercent`, `watchedEpisodes`, `totalEpisodes`, `shows[]` with `showId`, `title`, `watchedEpisodes`, `totalEpisodes`, `completionPercent`, `fullyWatched`. Shows ordered by `completionPercent` ascending. Exclude shows with zero episodes. Empty library → zeros and `shows: []`.

### Plan-to-watch count

**Decision:** `GET /api/analytics/plan-to-watch-count` returns `{ count }` — count of `user_library` rows where `library_status = PLAN_TO_WATCH` for the authenticated user.

Add `countByUserIdAndLibraryStatus` to `UserLibraryRepository`.

### REST API summary

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/analytics/watch-counts` | WATCHED counts by target type in period |
| GET | `/api/analytics/longest-to-watch` | Shows ranked by first-to-last episode watch span |
| GET | `/api/analytics/totals` | All-time WATCHED counts by target type |
| GET | `/api/analytics/favorites` | User-chosen favorites (Phase 5 delegate) |
| GET | `/api/analytics/watch-streaks` | Current + longest consecutive-day streaks |
| GET | `/api/analytics/library-completion` | Per-show + overall episode completion |
| GET | `/api/analytics/plan-to-watch-count` | Count of PLAN_TO_WATCH shows |

All endpoints require JWT auth via `@CurrentUser UUID userId`.

## Risks / Trade-offs

- **[Risk] Event-based totals inflate on re-mark cycles** → Acceptable per PRD; log is source of truth for time-based analytics
- **[Risk] Cross-user data leak** → All queries filter by JWT `userId`; integration tests with two users
- **[Risk] Streak timezone** → UTC only (consistent with watch-counts date bounds); document in OpenAPI
- **[Trade-off] Completion uses snapshot not log** → Correct for "how much is done now"; differs from event-log totals

## Migration Plan

1. Implement `analytics/` package; extend repositories
2. Add unit + integration tests; verify `./gradlew check` (JaCoCo ≥ 80%)
3. Update `OpenApiIntegrationTest` for seven paths
4. On archive: merge analytics delta spec; check off TASKS.md 6.1, 6.2, 6.3, 6.5

**Rollback:** Remove `analytics/` package; revert repository query methods.

## Open Questions

None — streak metrics (current + longest), completion source, and plan-to-watch scope confirmed during planning.
