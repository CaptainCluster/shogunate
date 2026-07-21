## Why

With auth in place, users need to discover TV shows and build a personal library before watch tracking, reviews, or analytics can attach to anything. Show library is the first user-visible product feature after authentication.

## What Changes

- Add Flyway migration for `shows`, `seasons`, `episodes` tables
- Implement TVmaze client (search + detail/snapshot fetch)
- Add show library REST API: search, add, list, detail, patch status, delete
- Add frontend library UI: search, add to library, library list, show detail
- Resolve open assumptions from PRD §9 for duplicate-add and delete behavior

## Capabilities

### New Capabilities

None — implementing existing `show` spec.

### Modified Capabilities

- `show`: Clarify duplicate-add rejection and delete cascade behavior via delta spec scenarios

## Non-goals

- Watch tracking, reviews, or favorites (later phases — delete cascade will be extended when those tables exist)
- Live-sync with TVmaze after initial snapshot
- Production TVmaze rate-limit tuning beyond basic retry

## Impact

- **Backend**: New `show/` package with TVmaze integration
- **Frontend**: New `features/library/` and `showApi.ts`
- **Config**: Optional `tvmaze.base-url` in `application-local.yml` (defaults to `https://api.tvmaze.com`; no API key)
- **Database**: V4 migration for show hierarchy tables

## Resolved Assumptions (PRD §9)

1. **Plan to Watch** applies only at show level — unchanged, matches spec
2. **Metadata is snapshotted once** at add-time — no background TVmaze polling
3. **Duplicate add**: Reject with `409 Conflict` if the user already has the same TVmaze show in their library
4. **Delete cascade**: Removing a show deletes its seasons/episodes; when watch/review/favorite tables exist in later phases, those rows for the show hierarchy will also be deleted (design prepares FK/cleanup hooks now where possible)
