## 1. Analytics Package Scaffold

- [x] 1.1 Add `AnalyticsPeriod` enum and DTO records: `WatchCountsResponse`, `TotalsResponse`, `LongestToWatchResponse`, `WatchStreaksResponse`, `LibraryCompletionResponse`, `PlanToWatchCountResponse`, `ShowCompletionResponse`
- [x] 1.2 Add `AnalyticsService` with period resolution (MONTH/YEAR derive `to` from `from`; CUSTOM requires `to`; reject invalid ranges with `ValidationException`) and streak calculation logic
- [x] 1.3 Add `AnalyticsController` at `/api/analytics` with `@CurrentUser`, `@Operation` summaries, and seven GET endpoints wired to service

## 2. Repository Queries

- [x] 2.1 Add `WatchEventRepository.countWatchedByTargetType(userId, from, to)` — GROUP BY `target_type`, filter `action = WATCHED`
- [x] 2.2 Add `WatchEventRepository.countWatchedByTargetTypeAllTime(userId)` — same grouping, no date filter
- [x] 2.3 Add native query `WatchEventRepository.findLongestToWatchByShow(userId)` — episode WATCHED events joined to shows, scoped to `user_library`, ordered by duration DESC
- [x] 2.4 Add `WatchEventRepository.findDistinctWatchedDates(userId)` — distinct UTC dates with WATCHED activity
- [x] 2.5 Add native query for library completion by show (join `user_library` → catalog → `user_watch_state`)
- [x] 2.6 Add `UserLibraryRepository.countByUserIdAndLibraryStatus(userId, status)`

## 3. Endpoint Implementation

- [x] 3.1 Implement `GET /api/analytics/watch-counts` — resolve period bounds, query counts, map missing target types to 0, echo resolved `from`/`to`
- [x] 3.2 Implement `GET /api/analytics/longest-to-watch` — ranked list with `showId`, `title`, `durationSeconds`, `firstWatchedAt`, `lastWatchedAt`
- [x] 3.3 Implement `GET /api/analytics/totals` — all-time WATCHED counts by target type
- [x] 3.4 Implement `GET /api/analytics/favorites` — delegate to `FavoriteService.listFavorites(userId)`
- [x] 3.5 Implement `GET /api/analytics/watch-streaks` — current + longest streak with start/end dates
- [x] 3.6 Implement `GET /api/analytics/library-completion` — overall + per-show completion, ordered incomplete-first
- [x] 3.7 Implement `GET /api/analytics/plan-to-watch-count` — count `PLAN_TO_WATCH` library rows only

## 4. Tests

- [x] 4.1 Add `AnalyticsServiceTest`: period resolution; count mapping; totals; longest-to-watch ordering; streak algorithm (consecutive days, gap, current=0 after inactivity); completion math; cross-user scoping via mocks
- [x] 4.2 Add `AnalyticsIntegrationTest`: full HTTP flows for all seven endpoints; period filter; two-user isolation; favorites vs suggestions; empty history; partial/full completion; plan-to-watch count
- [x] 4.3 Update `OpenApiIntegrationTest` — assert all seven `/api/analytics/*` paths with operation summaries
- [x] 4.4 Verify `./gradlew spotlessApply check` passes including JaCoCo ≥ 80% gate

## 5. Close-out

- [x] 5.1 Verify `/swagger-ui.html` lists all seven analytics endpoints with summaries
- [ ] 5.2 On archive: check off Phase 6 tasks 6.1, 6.2, 6.3, 6.5 in `openspec/TASKS.md`
