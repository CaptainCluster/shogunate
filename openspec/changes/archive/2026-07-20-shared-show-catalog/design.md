## Context

Auth provides JWT-scoped user identity. The current show spec and pending `show-library` change model show metadata as per-user snapshots (`shows.user_id`). No show tables or backend code exist yet. This design replaces that model with a global catalog shared across users, with per-user library membership and user-scoped activity data.

## Goals / Non-Goals

**Goals:**
- Shared global catalog (`shows`, `seasons`, `episodes`) keyed by `tvmaze_id`
- Per-user library via `user_library` (`library_status`, `added_at`)
- Per-user current watch state via `user_watch_state` (shared catalog entities have no `watched` columns)
- Add-show: create catalog on first add; reuse catalog for subsequent users
- Remove-show: delete user data; orphan-delete global catalog when no users remain
- Full show library API per updated `show` spec
- TVmaze proxied only through `show/tvmaze/TvmazeClient`
- User isolation on every query via `@CurrentUser`

**Non-Goals:**
- Watch/review/favorite table implementation (delete cleanup stubbed for future)
- Live-sync with TVmaze after initial catalog creation
- Persisting search results without explicit add
- Migrating per-user show data (none exists)

## Decisions

### Global catalog vs per-user snapshots

**Decision:** One show/season/episode tree per `tvmaze_id`, shared by all users.

**Alternative considered:** Per-user snapshots (current spec). Rejected — duplicates metadata unnecessarily; user activity tables already scope by `user_id`.

### User library junction table

**Decision:** `user_library(user_id, show_id, library_status, added_at)` with unique `(user_id, show_id)`.

`library_status` moves here from `shows`. Listing a user's library joins `user_library` → `shows`.

### Per-user watch state

**Decision:** `user_watch_state(user_id, target_type, target_id, watched, watched_at)` with PK `(user_id, target_type, target_id)`.

Shared catalog rows must not carry watched state — two users watching the same episode need independent state. The immutable `watch_events` log (future) references shared `target_id` values filtered by `user_id`.

### Add-show flow

1. Check if user already has `user_library` row for this `tvmaze_id` → `409 Conflict`
2. Look up global `shows` by `tvmaze_id`
3. If missing: fetch from TVmaze (`GET /shows/{id}` + `/episodes`), insert catalog in one transaction
4. Insert `user_library` row linking user to show
5. Return show detail (catalog + user's library fields)

Second user adding the same show skips TVmaze fetch and step 3.

### Remove-show flow

`ShowService.removeFromLibrary(userId, showId)` in one transaction:

1. Verify `user_library` exists for `(userId, showId)`
2. Collect all season/episode IDs in show hierarchy
3. Delete user's reviews, favorites, watch_events, `user_watch_state` for those targets (when tables exist; stub now)
4. Delete `user_library` row
5. If no remaining `user_library` rows for `showId`: delete episodes → seasons → show (orphan cleanup)

### Duplicate add: 409 Conflict

If `(user_id, show_id)` already exists in `user_library`, return `409` with message "Show already in library".

### Schema (V4 migration)

```sql
-- Global catalog (no user_id)
shows(id, tvmaze_id UNIQUE, title, overview, poster_url, first_air_date, created_at)
seasons(id, show_id REFERENCES shows, season_number, name, UNIQUE(show_id, season_number))
episodes(id, season_id REFERENCES seasons, episode_number, title, air_date,
         UNIQUE(season_id, episode_number))

-- Per-user library
user_library(id, user_id REFERENCES users, show_id REFERENCES shows,
             library_status, added_at, UNIQUE(user_id, show_id))

-- Per-user current watch state
user_watch_state(user_id REFERENCES users, target_type, target_id,
                 watched, watched_at, PRIMARY KEY (user_id, target_type, target_id))
```

Indexes: `user_library(user_id)`, `seasons(show_id)`, `episodes(season_id)`, `user_watch_state(user_id, target_type, target_id)`.

`library_status`: `NONE` | `PLAN_TO_WATCH` — Postgres check constraint on `user_library`.

### TVmaze client

Unchanged from `show-library`: search `GET /search/shows?q=`, snapshot `GET /shows/{id}` + `/episodes`, base URL from `tvmaze.base-url`, User-Agent header, retry on 429 (max 3), strip HTML from `summary`.

### Supersedes show-library

Do not apply `openspec/changes/show-library/`. This change replaces its schema and service design. Archive or cancel `show-library` after `shared-show-catalog` ships.

## Risks / Trade-offs

- **[Risk] Stale catalog metadata** — first user's snapshot is shared forever (no live sync). Mitigation: matches existing PRD assumption; refresh can be a future change.
- **[Risk] Orphan cleanup race** — two users remove the same show concurrently. Mitigation: single transaction; re-check count before catalog delete.
- **[Risk] TVmaze rate limit (20 calls / 10 sec)** — mitigated by catalog reuse (second user adds skip fetch).
- **[Trade-off] Cross-user shared UUIDs** — show/episode IDs are globally unique, not per-user. Queries must always verify library membership via `user_library`; never expose catalog detail without membership check.

## Migration Plan

1. V4 migration (V3 is `username_auth`)
2. Backend show package + tests (TVmaze mocked)
3. Update `docs/ARCHITECTURE.md`, `docs/PRD.md`, `openspec/AGENTS.md`
4. Frontend library feature
5. Archive `shared-show-catalog`; cancel/supersede `show-library`

## Open Questions

None — orphan cleanup confirmed: delete global catalog when last user removes the show.
