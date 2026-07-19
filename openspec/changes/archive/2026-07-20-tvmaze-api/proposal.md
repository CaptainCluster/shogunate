## Why

TMDB is a general movie/TV database with TV-specific limitations and proprietary API terms (API key, logo, disclaimer). TVmaze is TV-focused, requires no API key for the public API, and provides full episode lists in fewer calls — a better fit for a TV-only tracker before show-library implementation begins.

## What Changes

- Replace all TMDB references in specs, docs, and the pending `show-library` change with TVmaze equivalents
- **BREAKING:** Rename `tmdb_id` / `tmdbId` to `tvmaze_id` / `tvmazeId` in schema and API contracts
- Replace TMDB API compliance requirements with TVmaze CC BY-SA attribution and usage rules
- Update backend package naming: `show/tmdb/` → `show/tvmaze/` (`TvmazeClient`, `TvmazeMapper`, `TvmazeConfig`)
- Remove TMDB API key configuration; add optional `tvmaze.base-url` (defaults to `https://api.tvmaze.com`)
- Fix Flyway migration numbering in `show-library`: V4 for show tables (V3 is `username_auth`)

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `show`: Replace TMDB API compliance with TVmaze API compliance; update search/add scenarios to reference TVmaze-sourced metadata

## Non-goals

- Implementing the show library feature (remains in `show-library` change)
- Choosing a paid TVmaze premium tier
- Migrating any existing TMDB data (no TMDB integration has been implemented yet)

## Impact

- **Specs:** `openspec/specs/show/spec.md` (via delta merge on archive)
- **Pending change:** `openspec/changes/show-library/` (proposal, design, tasks, delta spec)
- **Docs:** `docs/PRD.md`, `docs/ARCHITECTURE.md`, `docs/TASKS.md`, `openspec/TASKS.md`, `openspec/AGENTS.md`
- **Future code:** Backend `show/tvmaze/` package, frontend attribution UI, no API key in config
