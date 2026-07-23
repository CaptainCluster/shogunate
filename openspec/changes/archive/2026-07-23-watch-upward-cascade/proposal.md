## Why

Watch state currently cascades downward only in the live spec and human docs: marking a season or show watched propagates to descendants, but marking episodes individually leaves season/show rows unwatched even when every episode is done. Backend code now implements upward auto-marking; the spec and architecture docs must catch up so behavior is documented, testable, and reviewable.

## What Changes

- Add upward cascade requirements to the watch spec: auto-mark season when all its episodes are watched; auto-mark show when all episodes are watched
- Parent `watchedAt` MUST equal the latest episode `watchedAt` in the completed hierarchy
- Unmarking an episode MUST clear season/show watch state when the hierarchy is no longer fully watched
- Upward promotions/demotions MUST emit cascade-tagged history log entries referencing the initiating user action
- Update `docs/ARCHITECTURE.md` and `docs/PRD.md` to describe bidirectional cascade and `WatchHierarchySyncService`
- Verify existing backend implementation and tests align with the updated spec (no API or frontend changes expected)

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

- `watch`: Add upward mark/unmark cascade requirements, timestamp rules, and cascade event tagging for parent promotions

## Impact

- **Spec:** `openspec/specs/watch/spec.md` (via delta merge on archive)
- **Docs:** `docs/ARCHITECTURE.md` §5, `docs/PRD.md` §5.3
- **Backend:** `WatchHierarchySyncService`, `WatchService` — already implemented; tasks focus on spec/doc alignment and test verification
- **Frontend:** No changes — show detail already refetches after watch mutations
- **API:** No endpoint or contract changes

## Non-goals

- Changing downward cascade semantics or timestamp preservation rules
- New REST endpoints or frontend UI for upward cascade
- Analytics query changes (episode-level events remain the source of truth for time-based metrics)
- Auto-sync when new catalog episodes are added after a season/show was marked watched
