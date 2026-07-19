## Context

Auth provides JWT-scoped user identity. Show spec requires TVmaze-backed search (non-persistent) and snapshot-on-add library entries per user. Foundation uses Flyway per-feature migrations.

## Goals / Non-Goals

**Goals:**
- Full show library API per `openspec/specs/show/spec.md`
- TVmaze proxied only through `show/tvmaze/TvmazeClient`
- User isolation on every query via `@CurrentUser`
- Frontend search → add → list → detail flow

**Non-Goals:**
- Watch/review/favorite tables (delete cleanup stubbed/documented for future)
- Caching search results in DB

## Decisions

### Duplicate add: 409 Conflict
If `shows(user_id, tvmaze_id)` unique constraint violated, return `409` with message "Show already in library". Prevents silent duplicates.

### Delete: explicit service-layer cascade
`ShowService.deleteShow(userId, showId)` deletes episodes → seasons → show in one transaction. Future watch/review/favorite cleanup will extend this method.

### Schema
```sql
shows(id, user_id, tvmaze_id UNIQUE per user, title, overview, poster_url, first_air_date, library_status, created_at)
seasons(id, show_id, season_number, name)
episodes(id, season_id, episode_number, title, air_date)
```
Unique: `(user_id, tvmaze_id)` on shows; `(show_id, season_number)` on seasons; `(season_id, episode_number)` on episodes.

### TVmaze client
- Search: `GET /search/shows?q={query}`
- Snapshot: `GET /shows/{id}` + `GET /shows/{id}/episodes`
- Base URL from `tvmaze.base-url` property (default `https://api.tvmaze.com`)
- Descriptive `User-Agent` header on all requests
- Simple retry on 429 with exponential backoff (max 3 attempts)
- Strip HTML from TVmaze `summary` field in `TvmazeMapper`

### library_status enum
`NONE` | `PLAN_TO_WATCH` — Postgres check constraint

## Risks / Trade-offs

- **[Risk] TVmaze rate limit (20 calls / 10 sec)** → Backoff on 429; single episodes call per add
- **[Risk] Large shows mean many rows on add** → Acceptable for MVP; batch insert in one transaction
- **[Risk] TVmaze summary contains HTML** → Strip in mapper before persistence

## Migration Plan

1. V4 migration (V3 is `username_auth`)
2. Backend show package + tests (TVmaze mocked)
3. Frontend library feature
4. Manual E2E with real TVmaze API optional

## Open Questions

None — assumptions resolved in proposal.
