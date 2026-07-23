## Context

Watch cascade today is documented and implemented as **downward only**: marking a season/show propagates to episodes (and seasons for a show). `LibraryStatusSyncService` already sets library `WATCHED` when all episodes are watched, but season/show rows in `user_watch_state` were not auto-updated — so show detail could show `season.watched = false` despite 100% episode progress.

`WatchHierarchySyncService` has been added and wired from `WatchService` after every mark/unmark, within the same transaction as downward cascade and library sync. This change formalizes that behavior in spec and human docs.

## Goals / Non-Goals

**Goals:**

- Document upward mark cascade: season/show auto-marked when all descendant episodes are watched
- Document parent timestamp rule: `watchedAt` = max episode `watchedAt` in the completed scope
- Document upward unmark cascade: clear season/show when hierarchy is no longer fully watched
- Document cascade event tagging for upward promotions/demotions
- Align `docs/ARCHITECTURE.md` and `docs/PRD.md` with the implemented flow

**Non-Goals:**

- API or frontend changes
- Analytics computation changes
- Handling newly added catalog episodes after a parent was auto-marked

## Decisions

### 1. Dedicated `WatchHierarchySyncService` (not inline in `WatchService`)

**Choice:** Upward sync lives in its own service, called from `WatchService` after downward cascade and event logging.

**Rationale:** Keeps `WatchService` focused on user-initiated targets; upward logic is testable in isolation (`WatchHierarchySyncServiceTest`). Mirrors the existing split with `LibraryStatusSyncService`.

**Alternative considered:** Private methods on `WatchService` — rejected for testability and file size.

### 2. Parent timestamp = latest episode timestamp

**Choice:** When promoting a season or show, set `watchedAt` to the maximum `watchedAt` among watched episodes in scope (equivalently, the most recently watched episode).

**Rationale:** Matches user expectation that "finishing a season" happens when the last episode is marked, not when an earlier parent row might have existed.

**Alternative considered:** Use `Instant.now()` at promotion time — rejected; loses alignment with actual completion moment.

### 3. Upward events reference the initiating user action

**Choice:** Upward promotion/demotion events use `triggeredByCascade = true` and `cascadeSourceId` pointing to the top-level event from the user's mark/unmark (typically the episode event).

**Rationale:** Consistent with existing cascade tagging; preserves audit trail without treating auto-promotion as a separate user action.

### 4. Transaction boundary unchanged

**Choice:** Downward cascade → history log → upward sync → library sync, all in one `@Transactional` on `WatchService.markWatched` / `unmarkWatched`.

**Rationale:** Matches AGENTS.md non-negotiable: no partial cascades.

## Risks / Trade-offs

- **[New catalog episodes after completion]** → Out of scope for MVP; a show can appear "fully watched" until new episodes arrive. Same gap exists for library `WATCHED` status.
- **[Duplicate promotion events]** → `promoteIfNeeded` skips when parent already watched with matching timestamp; only logs when state actually changes.
- **[Spec/docs drift]** → Tasks include explicit verification that existing tests cover delta scenarios before archive.

## Migration Plan

No database migration. Deploy is code-only (already present). Archive change merges spec delta into `openspec/specs/watch/spec.md` and updates human docs in the same commit.

## Open Questions

None — behavior is implemented and covered by integration tests.
