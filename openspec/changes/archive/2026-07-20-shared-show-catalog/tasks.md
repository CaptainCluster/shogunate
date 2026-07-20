## 1. Database

- [x] 1.1 Add Flyway V4 migration: global `shows`, `seasons`, `episodes` (no `user_id`; unique `tvmaze_id` on shows)
- [x] 1.2 Add `user_library` table with `library_status`, `added_at`, unique `(user_id, show_id)`
- [x] 1.3 Add `user_watch_state` table with PK `(user_id, target_type, target_id)`

## 2. Backend TVmaze & Show Core

- [x] 2.1 Add TvmazeConfig and TvmazeClient with search and snapshot fetch
- [x] 2.2 Add Show, Season, Episode, UserLibrary, UserWatchState entities and repositories
- [x] 2.3 Implement ShowService: search (ephemeral), add (create catalog or reuse + library link), list (join user_library), detail (membership check), patch library status, remove (user cleanup + orphan delete)
- [x] 2.4 Implement ShowController REST endpoints scoped via `@CurrentUser`
- [x] 2.5 Document delete hooks for future watch/review/favorite cleanup in ShowService.removeFromLibrary

## 3. Backend Tests

- [x] 3.1 Unit tests for TvmazeMapper and duplicate-add rejection (409)
- [x] 3.2 Integration tests: first add creates catalog; second user reuses catalog without TVmaze call
- [x] 3.3 Integration tests: remove by one user preserves catalog for others; orphan delete when last user removes

## 4. Frontend Library

- [x] 4.1 Add showApi.ts
- [x] 4.2 Add search page and library list
- [x] 4.3 Add show detail page with seasons/episodes
- [x] 4.4 Add About/Credits section with TVmaze attribution and CC BY-SA notice

## 5. Documentation

- [x] 5.1 Update docs/ARCHITECTURE.md ER diagram and snapshot/add/remove flows for shared catalog
- [x] 5.2 Update docs/PRD.md data model note (shared catalog, not per-user copies)
- [x] 5.3 Update openspec/AGENTS.md isolation constraints for shared catalog + user_library scoping

## 6. Verification & Cleanup

- [x] 6.1 Verify ./gradlew test passes
- [x] 6.2 Verify pnpm build passes
- [x] 6.3 Mark openspec/changes/show-library as superseded (do not apply); archive shared-show-catalog when complete
