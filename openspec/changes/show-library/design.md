## Context

Auth provides JWT-scoped user identity. Show spec requires TMDb-backed search (non-persistent) and snapshot-on-add library entries per user. Foundation uses Flyway per-feature migrations.

## Goals / Non-Goals

**Goals:**
- Full show library API per `openspec/specs/show/spec.md`
- TMDb proxied only through `show/tmdb/TmdbClient`
- User isolation on every query via `@CurrentUser`
- Frontend search → add → list → detail flow

**Non-Goals:**
- Watch/review/favorite tables (delete cleanup stubbed/documented for future)
- Caching search results in DB

## Decisions

### Duplicate add: 409 Conflict
If `shows(user_id, tmdb_id)` unique constraint violated, return `409` with message "Show already in library". Prevents silent duplicates.

### Delete: explicit service-layer cascade
`ShowService.deleteShow(userId, showId)` deletes episodes → seasons → show in one transaction. Future watch/review/favorite cleanup will extend this method.

### Schema
```sql
shows(id, user_id, tmdb_id UNIQUE per user, title, overview, poster_url, first_air_date, library_status, created_at)
seasons(id, show_id, season_number, name)
episodes(id, season_id, episode_number, title, air_date)
```
Unique: `(user_id, tmdb_id)` on shows; `(show_id, season_number)` on seasons; `(season_id, episode_number)` on episodes.

### TMDb client
- Search: `GET /search/tv`
- Snapshot: `GET /tv/{id}` + iterate seasons with `GET /tv/{id}/season/{n}`
- API key from `tmdb.api-key` property
- Simple retry on 429 with exponential backoff (max 3 attempts)

### library_status enum
`NONE` | `PLAN_TO_WATCH` — Postgres check constraint

## Risks / Trade-offs

- **[Risk] TMDb API key required for dev** → Document in `.env.example` / application-local.yml placeholder
- **[Risk] Large shows mean many rows on add** → Acceptable for MVP; batch insert in one transaction

## Migration Plan

1. V3 migration
2. Backend show package + tests (TMDb mocked)
3. Frontend library feature
4. Manual E2E with real TMDb key optional

## Open Questions

None — assumptions resolved in proposal.
