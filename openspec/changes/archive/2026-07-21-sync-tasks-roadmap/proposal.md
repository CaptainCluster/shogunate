## Why

`openspec/TASKS.md` still describes the original per-user show model and marks Phases 0–2 incomplete, even though those phases shipped via archived OpenSpec changes (username auth, TVmaze, shared catalog). Agents and humans cannot trust it for progress or acceptance criteria, which leads to wrong assumptions during planning and implementation.

## What Changes

- Restore `openspec/TASKS.md` as the **live phase-level roadmap** (replace the "historical reference" header).
- Mark Phases 0–2 complete (`[x]`).
- Rewrite Phase 2 task text for the shared global catalog + `user_library` model and TVmaze integration.
- Clarify Phase 3: `user_watch_state` exists from V4; Phase 3 adds `watch_events` and cascade logic.
- Fix Phase 7.4 E2E flow (`register → login`, not verify) and update sequencing notes with resolved decisions.
- Update `openspec/AGENTS.md` so workflow references `openspec/TASKS.md` as the canonical phase checklist.

## Capabilities

### New Capabilities

None — documentation-only change; no new behavioral requirements.

### Modified Capabilities

None — no delta specs; behavior remains defined in existing `openspec/specs/`.

## Impact

- **Docs:** `openspec/TASKS.md`, `openspec/AGENTS.md`
- **No code, API, or migration changes**
- **`docs/TASKS.md` unchanged** (remains historical)

## Non-goals

- Editing `docs/TASKS.md`
- Implementing Phase 3+ features
- Creating or modifying delta specs under `openspec/specs/`
