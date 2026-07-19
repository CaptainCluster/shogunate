## Context

Show metadata is currently specified against TMDB across `openspec/specs/show/spec.md`, `docs/`, and the pending `show-library` change. No TMDB integration code exists yet — this pivot is documentation and spec only, applied before show-library implementation.

TVmaze provides a free public REST API (CC BY-SA 4.0) focused on TV listings and metadata. No API key is required.

## Goals / Non-Goals

**Goals:**
- Replace all TMDB references with TVmaze across specs, docs, and `show-library` artifacts
- Document TVmaze endpoint mapping, field mapping, and compliance rules
- Align naming: `tvmaze_id`, `show/tvmaze/TvmazeClient`, etc.

**Non-Goals:**
- Implementing show library code (deferred to `show-library`)
- Paid TVmaze premium tier
- Data migration (no TMDB data exists)

## Decisions

### TVmaze endpoints (replaces TMDB)

| Operation | TVmaze endpoint |
|---|---|
| Search | `GET /search/shows?q={query}` |
| Show metadata | `GET /shows/{id}` |
| Full episode list | `GET /shows/{id}/episodes` |

Alternative considered: TMDB per-season iteration (`GET /tv/{id}/season/{n}`). Rejected — TVmaze returns all episodes in one call, reducing API calls and simplifying snapshot logic.

### Field mapping

| App field | TVmaze source |
|---|---|
| `tvmaze_id` | `id` |
| `title` | `name` |
| `overview` | `summary` (strip HTML before store/display) |
| `poster_url` | `image.medium` or `image.original` |
| `first_air_date` | `premiered` |
| `season_number` | episode `season` |
| `episode_number` | episode `number` |
| episode `title` | episode `name` |
| episode `air_date` | episode `airdate` |

### Package and config naming

- `show/tmdb/` → `show/tvmaze/` (`TvmazeClient`, `TvmazeMapper`, `TvmazeConfig`)
- Remove `tmdb.api-key` property
- Add optional `tvmaze.base-url` (default `https://api.tvmaze.com`)
- Set descriptive `User-Agent` header on all TVmaze requests

### Compliance approach

- Backend proxies all TVmaze calls (consistent architecture, no key to protect)
- CC BY-SA attribution: link to TVmaze in About/Credits; per-show links via API `url` field where metadata is displayed
- Handle HTTP 429 with exponential backoff (max 3 attempts)
- Hotlink poster URLs from TVmaze CDN; do not re-host as general-purpose CDN
- Search results never persisted; library snapshots only on explicit add

### Flyway migration numbering

`show-library` referenced V3 for show tables, but `V3__username_auth.sql` already exists. Show tables use **V4**.

## Risks / Trade-offs

- **[Risk] CC BY-SA ShareAlike on adapted data** → Mitigation: display metadata as-is with attribution; do not create derivative datasets for redistribution
- **[Risk] TVmaze `summary` contains HTML** → Mitigation: strip HTML in `TvmazeMapper` before persistence
- **[Risk] Rate limit (20 calls / 10 sec per IP)** → Mitigation: backoff on 429; single `/episodes` call per add instead of per-season TMDB calls
- **[Trade-off] TV-only API** → Acceptable — product scope is TV-only

## Migration Plan

1. Create and archive `tvmaze-api` change (spec delta merge)
2. Update `show-library` artifacts to reference TVmaze
3. Update docs and AGENTS.md
4. Implement show library via `show-library` using TVmaze from the start

## Open Questions

None.
