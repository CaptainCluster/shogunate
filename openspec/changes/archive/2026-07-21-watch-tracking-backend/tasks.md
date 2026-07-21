## 1. Database Migrations

- [x] 1.1 Verify Flyway V4 exists (`V4__shared_show_catalog.sql`) — do not add a second V4; catalog tables already defined in Phase 2
- [x] 1.2 Verify V4 runs cleanly on a fresh Postgres (`./gradlew flywayMigrate` or app startup with `ddl-auto: validate`)
- [x] 1.3 Add Flyway V5 migration: `watch_events` table with indexes per design.md

## 2. Watch Package Scaffold

- [x] 2.1 Create `com.tvtracker.watch/` package with `WatchAction` enum (`WATCHED`, `UNWATCHED`)
- [x] 2.2 Add `WatchEvent` entity and `WatchEventRepository` (save-only for normal ops; bulk delete query for library removal)
- [x] 2.3 Move `UserWatchState` and `UserWatchStateRepository` from `show/` to `watch/`; fix all imports

## 3. WatchService Cascade Logic

- [x] 3.1 Implement target resolution and library membership verification (episode → season → show chain)
- [x] 3.2 Implement `markWatched(userId, targetType, targetId)`: upsert state, append events, cascade tagging, single transaction
- [x] 3.3 Implement `unmarkWatched(userId, targetType, targetId, confirm)`: reject season/show without `confirm=true`; cascade down; append UNWATCHED events
- [x] 3.4 Handle re-mark: update existing `user_watch_state` timestamp; still append new WATCHED event

## 4. Watch REST API

- [x] 4.1 Add `WatchController` with `POST`/`DELETE` on `/api/watch/episodes/{id}`, `/api/watch/seasons/{id}`, `/api/watch/shows/{id}`
- [x] 4.2 Wire `@CurrentUser`, return `204 No Content` on success; OpenAPI `@Operation` annotations
- [x] 4.3 Map errors: `404` not found, `403` not in library, `400` missing confirm

## 5. Show Integration

- [x] 5.1 Extend `EpisodeResponse`, `SeasonResponse`, `ShowDetailResponse` with `watched` and `watchedAt`
- [x] 5.2 Update `ShowService.getShowDetail` to batch-load `user_watch_state` for show hierarchy
- [x] 5.3 Update `ShowService.removeFromLibrary` to delete user's `watch_events` for hierarchy target IDs (in addition to existing `user_watch_state` cleanup)

## 6. Test Coverage (JaCoCo ≥ 80%)

- [x] 6.1 Add JaCoCo plugin to `build.gradle.kts` with `jacocoTestCoverageVerification` (≥ 80% line coverage, exclusions: `TvTrackerApplication`, `**/dto/**`, `**/config/**`) wired into `./gradlew check`
- [x] 6.2 Add `WatchServiceTest`: mark episode, show cascade + shared timestamp, re-mark updates timestamp, unmark without confirm fails, unmark with confirm clears hierarchy, cascade event flags
- [x] 6.3 Add `WatchIntegrationTest`: auth + add show + mark/unmark HTTP flows; confirm rejection; cross-user isolation
- [x] 6.4 Update `ShowServiceTest` / `ShowIntegrationTest`: detail includes watch fields; remove deletes watch_events
- [x] 6.5 Add cascade atomicity test (partial failure rolls back state and events)
- [x] 6.6 Verify `./gradlew check` passes including coverage gate

## 7. Documentation

- [x] 7.1 Update `openspec/TASKS.md` Phase 3: note backend scope for this change, defer 3.5 frontend, extend 3.6 with JaCoCo acceptance, add 3.4b show detail read path
- [x] 7.2 Verify `/swagger-ui.html` lists all six watch endpoints with summaries
