## Context

Phases 0–2 shipped auth and show library against a shared global catalog. `UserWatchState` entity and repository exist under `show/` today, but no `watch/` package, no `watch_events` table, and no cascade logic. [openspec/specs/watch/spec.md](../../specs/watch/spec.md) defines behavioral requirements; this change implements them on the backend only.

**Constraint:** Flyway V4 (`V4__shared_show_catalog.sql`) already ships the catalog schema from Phase 2. This change adds **V5** only — never a second V4 migration.

## Goals / Non-Goals

**Goals:**
- `watch/` vertical slice: controller → service → repositories → entities
- Atomic cascade mark/unmark updating `user_watch_state` and appending `watch_events`
- REST endpoints scoped via `@CurrentUser` and library membership
- Show detail read path includes watch state for frontend Phase 3.5
- JaCoCo ≥ 80% line coverage enforced locally in `./gradlew check`
- Complete Phase 3 backend tasks 3.1–3.4 and 3.6

**Non-Goals:**
- Frontend UI (Phase 3.5)
- Analytics over `watch_events` (Phase 6)
- SonarQube / SonarCloud
- Dedicated watch status GET endpoints

## Decisions

### Package layout: new `watch/`; move `UserWatchState*`

**Decision:** Create `com.tvtracker.watch/` with `WatchController`, `WatchService`, `WatchEvent`, `WatchEventRepository`, `WatchAction` enum. Move `UserWatchState` and `UserWatchStateRepository` from `show/` to `watch/`.

**Rationale:** ARCHITECTURE §2.1 assigns cascade logic to `WatchService` in `watch/`. Colocating current-state entities with watch domain reduces coupling in `ShowService`.

**Alternative:** Keep `UserWatchState` in `show/` — rejected; `ShowService` already has watch cleanup TODOs and the package is overloaded.

### Migrations: V5 only (V4 pre-existing)

**Decision:** Reuse existing `V4__shared_show_catalog.sql` for global catalog tables. V5 adds `watch_events` only.

```sql
-- V5
CREATE TABLE watch_events (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type TEXT NOT NULL,
    target_id UUID NOT NULL,
    action TEXT NOT NULL CHECK (action IN ('WATCHED', 'UNWATCHED')),
    occurred_at TIMESTAMPTZ NOT NULL,
    triggered_by_cascade BOOLEAN NOT NULL DEFAULT FALSE,
    cascade_source_id UUID REFERENCES watch_events(id)
);
CREATE INDEX idx_watch_events_user_occurred ON watch_events(user_id, occurred_at);
CREATE INDEX idx_watch_events_user_target ON watch_events(user_id, target_type, target_id);
```

### Cascade algorithm in `WatchService`

**Decision:** Single `@Transactional` method per mark/unmark operation.

**Mark watched:**
1. Resolve target (`EPISODE` | `SEASON` | `SHOW`) and verify library membership
2. Collect affected targets (episode only; season + episodes; show + all seasons + episodes)
3. Upsert `user_watch_state` (`watched=true`, shared `watchedAt=now()`) for all targets
4. Insert top-level `watch_events` row first; use its `id` as `cascadeSourceId` for descendants
5. Child events: `triggered_by_cascade=true`, `cascade_source_id=cascadeSourceId`
6. Top-level event: `triggered_by_cascade=false`, `cascade_source_id=null`

**Unmark watched:**
1. Same resolution + membership check
2. Episode: no confirm required
3. Season/show: reject with `400` unless `confirm=true` query parameter
4. Set `watched=false`, clear `watchedAt` for target + descendants
5. Append `UNWATCHED` events with same cascade tagging

**Alternative:** Separate service methods per target type with shared helper — accepted as implementation detail inside `WatchService`.

### API contract

| Method | Path | Success | Notes |
|--------|------|---------|-------|
| POST | `/api/watch/episodes/{id}` | 204 | Mark episode |
| DELETE | `/api/watch/episodes/{id}` | 204 | Unmark episode |
| POST | `/api/watch/seasons/{id}` | 204 | Mark season cascade |
| DELETE | `/api/watch/seasons/{id}?confirm=true` | 204 | Unmark cascade |
| POST | `/api/watch/shows/{id}` | 204 | Mark show cascade |
| DELETE | `/api/watch/shows/{id}?confirm=true` | 204 | Unmark cascade |

Errors: `401` unauthenticated, `403` not in library, `404` target not found, `400` missing confirm.

### Read path via show detail enrichment

**Decision:** Extend `ShowDetailResponse`, `SeasonResponse`, `EpisodeResponse` with `watched` (boolean) and `watchedAt` (Instant, nullable). Batch-load `user_watch_state` in `ShowService.getShowDetail`.

**Rationale:** Frontend show detail page is the primary watch UI surface; avoids separate watch GET endpoints.

### Append-only `watch_events` with library-removal exception

**Decision:** `WatchEventRepository` exposes only `save` / `saveAll` for normal operations. Bulk delete by `(userId, targetIds)` is allowed only from `ShowService.removeFromLibrary`.

**Rationale:** Matches show spec cleanup requirement; AGENTS.md append-only rule applies to watch/unwatch flows, not user data deletion on library remove.

### Coverage: JaCoCo in Gradle (not SonarQube)

**Decision:** Add `jacoco` plugin; `jacocoTestCoverageVerification` on `./gradlew check` with **≥ 80% line coverage** for `com.tvtracker.**`.

**Exclusions:** `TvTrackerApplication`, `**/dto/**`, `**/config/**`.

**Rationale:** SonarQube requires a hosted server; JaCoCo provides local enforcement without external dependencies.

## Risks / Trade-offs

- **[Risk] Duplicate V4 migrations** → Use only `V4__shared_show_catalog.sql`; never add a second V4 file
- **[Risk] Cascade partial failure** → Single transaction; integration test asserts rollback on forced failure
- **[Risk] 80% coverage on existing code** → WatchServiceTest + WatchIntegrationTest; expand show/auth tests if needed
- **[Risk] Package move breaks imports** → Move in one step; run `./gradlew test` immediately
- **[Trade-off] Immutable log vs library cleanup** → Documented exception only in remove-from-library path

## Migration Plan

1. Confirm V4 (`V4__shared_show_catalog.sql`) is present; run Flyway on fresh Postgres
2. Add V5 `watch_events`
3. Implement `watch/` package; move `UserWatchState*`
4. Update show detail DTOs and remove cleanup
5. Add JaCoCo gate and tests
6. Verify `./gradlew check` passes

**Rollback:** Revert migration V5; remove watch package; restore `UserWatchState` in `show/` if moved.

## Open Questions

None — show detail enrichment and JaCoCo (not SonarQube) confirmed during planning.
