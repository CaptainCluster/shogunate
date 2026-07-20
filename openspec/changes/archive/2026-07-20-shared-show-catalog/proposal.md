## Why

The current show spec stores a full TVmaze snapshot per user, duplicating show/season/episode rows for every library member. A shared catalog keyed by `tvmaze_id` eliminates redundant metadata while keeping reviews, watch history, and favorites per-user and isolated. This pivot happens before `show-library` is implemented, so no migration of live data is required.

## What Changes

- **BREAKING**: Show metadata (`shows`, `seasons`, `episodes`) becomes a global shared catalog — no `user_id` on catalog tables
- Add `user_library` table linking users to shared shows (`library_status`, `added_at`)
- Add `user_watch_state` for per-user current watched state on shared catalog entities (replaces `watched`/`watched_at` on catalog rows)
- Add-show flow: first user creates catalog from TVmaze; later users reuse existing catalog and only get a library link
- Remove-from-library: delete user's library entry and user-scoped data; delete global catalog only when no users remain (orphan cleanup)
- Supersedes pending `show-library` change — do not apply `show-library` as-is

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `show`: Shared catalog persistence, user library membership, revised add/remove behavior, orphan cleanup when last user removes a show

## Non-goals

- Live-sync with TVmaze after initial catalog creation
- Persisting search results without an explicit add
- Watch/review/favorite feature implementation (later phases — schema and delete hooks are prepared)
- Migrating existing per-user show data (none exists yet)

## Impact

- **Specs**: `openspec/specs/show/spec.md` — persistence, add, and remove requirements change
- **Docs**: `docs/ARCHITECTURE.md` (ER diagram, snapshot flow), `docs/PRD.md` (data model note), `openspec/AGENTS.md` (isolation constraints)
- **Database**: V4 migration uses global catalog + `user_library` + `user_watch_state` instead of per-user `shows`
- **Backend**: New `show/` package (replaces `show-library` design)
- **Frontend**: Library UX unchanged; show IDs reference shared catalog entries
- **Blocked change**: `openspec/changes/show-library/` superseded — archive or cancel after this change ships
