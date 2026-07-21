## Context

`openspec/TASKS.md` was marked historical and never updated after Phases 0–2 shipped. Requirement pivots (username auth, TVmaze, shared show catalog) left Phase 2 descriptions and checkboxes wrong. `openspec/AGENTS.md` tells agents not to update TASKS.md, contradicting the desired live-roadmap role.

## Goals / Non-Goals

**Goals:**

- Re-establish two-level tracking: phase roadmap in `openspec/TASKS.md`, per-feature steps in change `tasks.md`, behavior in `openspec/specs/`.
- Mark Phases 0–2 complete with accurate acceptance criteria aligned to archived changes and current code.
- Update Phase 3+ text where it assumes per-user catalog rows or omits `user_watch_state`.
- Align `openspec/AGENTS.md` workflow with the live roadmap.

**Non-Goals:**

- Code, migrations, or API changes
- Editing `docs/TASKS.md`
- Delta specs (no behavioral changes)

## Decisions

### Live roadmap header

Replace the historical disclaimer with a status block stating `openspec/TASKS.md` is the canonical phase checklist, with pointers to change `tasks.md` and `openspec/specs/`.

**Alternative considered:** Keep historical header and only fix text — rejected; user explicitly chose live roadmap.

### Phase 2 rewrite scope

Rewrite tasks 2.1–2.7 descriptions and acceptance criteria for shared catalog model per `shared-show-catalog` design and `openspec/specs/show/spec.md`. Check all Phase 2 boxes.

### Phase 3 split

Add intro note that `user_watch_state` (V4) is done in Phase 2. Phase 3.1 covers `watch_events` only; 3.2 updates both `user_watch_state` and `watch_events`.

### AGENTS.md tracking rule

When archiving a change that completes phase task(s), check corresponding boxes in `openspec/TASKS.md` in the same commit.

### No delta specs

Proposal lists no modified capabilities; create `specs/README.md` only (docs-only change).

## Risks / Trade-offs

- **[Risk] `docs/TASKS.md` drifts from `openspec/TASKS.md`** → Mitigation: AGENTS.md clarifies `docs/TASKS.md` is historical; live roadmap is `openspec/TASKS.md` only (per user choice).
- **[Risk] Phase task IDs in commit messages still reference stale acceptance text** → Mitigation: rewritten criteria match implemented behavior.

## Migration Plan

1. Edit `openspec/TASKS.md` and `openspec/AGENTS.md`.
2. Verify with grep for stale phrases.
3. Archive change (no spec merge).

## Open Questions

None — resolved decisions documented in Sequencing notes; only remaining product assumption is show-level-only `PLAN_TO_WATCH` (already implemented).
