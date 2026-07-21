## Context

Phase 3 backend is complete: six watch mutation endpoints, show detail enriched with `watched` / `watchedAt`, and cascade logic in `WatchService`. The frontend show detail page ([`ShowDetailPage.tsx`](frontend/src/features/library/ShowDetailPage.tsx)) lists seasons and episodes but has no watch controls. TypeScript types in [`showApi.ts`](frontend/src/api/showApi.ts) omit watch fields even though the API already returns them.

Architecture ([`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) §7.1–7.3) defines the target layout: `api/watchApi.ts`, `features/watch/`, shared `useConfirm` for cascade unmark, TanStack Query for all server state.

## Goals / Non-Goals

**Goals:**
- Wire mark/unmark at show, season, and episode level on show detail using separate Mark / Unmark buttons
- Confirmation modal before season/show unmark; episode unmark fires immediately
- Derived season progress on detail (`watchedCount/totalCount` episodes)
- TanStack Query mutations with show-detail cache invalidation after cascade actions
- Vitest + Testing Library for confirm flow and watch mutation hooks
- Complete Phase 3.5 in `openspec/TASKS.md`

**Non-Goals:**
- Library list progress indicators
- Optimistic cache updates
- Analytics query invalidation (Phase 6)
- Backend changes

## Decisions

### API layer: `watchApi.ts` mirrors backend endpoints

**Decision:** Six typed functions wrapping `POST`/`DELETE` on `/api/watch/{episodes|seasons|shows}/{id}`. Season/show unmark accept `confirm: boolean` and append `?confirm=true` when true.

**Rationale:** Matches existing `showApi.ts` / `authApi.ts` pattern; pages never call `fetch` directly.

### Server state: mutations invalidate show detail only

**Decision:** All watch hooks live in `features/watch/hooks/`. On mutation success, invalidate `showKeys.detail(showId)` from [`showKeys.ts`](frontend/src/features/library/showKeys.ts).

**Rationale:** Watch state for the entire show hierarchy is loaded in one `GET /api/shows/{id}` response. A single invalidation refreshes all cascade-affected rows without optimistic merge logic.

**Alternative:** Optimistic `setQueryData` — rejected for MVP; invalidation matches existing library hooks and is simpler to test.

### Control pattern: separate Mark / Unmark buttons

**Decision:** `WatchButtonPair` shows **Mark watched** when `watched === false`, **Unmark** when `watched === true`. Never both at once.

**Rationale:** User preference from planning; makes destructive unmark visually distinct and pairs naturally with confirmation for season/show.

### Confirmation: shared `useConfirm` + `ConfirmDialog`

**Decision:** `ConfirmProvider` at app root exposes `confirm({ title, message }) → Promise<boolean>`. Season/show unmark awaits confirmation before calling `DELETE ?confirm=true`. Episode unmark skips the modal.

**Copy:**
- Season: "Unmark this season and all {n} episodes as unwatched?"
- Show: "Unmark this show and all {seasonCount} seasons ({episodeCount} episodes) as unwatched?"

**Rationale:** ARCHITECTURE §7.3; reusable for future destructive actions (delete review, remove from library refactor).

### Component placement on show detail

```
Show header     → WatchButtonPair (show) + existing library status / remove
Season header   → SeasonProgress + WatchButtonPair (season)
Episode row     → title/meta + WatchButtonPair (episode)
```

**Rationale:** Show-level bulk action near title; season controls adjacent to progress; episode controls per row.

### Season progress: client-derived, detail page only

**Decision:** `SeasonProgress` computes `episodes.filter(e => e.watched).length / episodes.length`. Not sent to API; not shown on library list.

**Rationale:** Backend does not roll up partial season state; derived count gives UX value without new endpoints.

### Testing: Vitest setup + three test files

**Decision:** Add Vitest, jsdom, Testing Library to `frontend/package.json`. Test `useConfirm`, `useWatchMutations` (mocked API + query invalidation), `WatchButtonPair` (confirm gating for season unmark).

**Rationale:** Architecture §8.3; user requested tests in planning. Mock `watchApi` — no live backend in unit tests.

## Risks / Trade-offs

- **[Risk] Invalidation flicker on large shows** → Accept for MVP; optimistic updates deferred
- **[Risk] Modal accessibility gaps** → Use `role="dialog"`, labelled title, Escape to cancel, focus management on open
- **[Risk] Pending state disables wrong buttons** → Track pending target id + action in mutation hook
- **[Trade-off] No library progress** → Users must open show detail to see watch state; acceptable per scope

## Migration Plan

1. Extend `showApi.ts` types; add `watchApi.ts`
2. Add `useConfirm` provider and `ConfirmDialog`
3. Add `features/watch/` hooks and components
4. Integrate into `ShowDetailPage`
5. Add Vitest tooling and tests
6. Verify: `pnpm lint`, `pnpm build`, `pnpm test:run`; manual smoke on show detail
7. Archive change; check off TASKS.md 3.5

**Rollback:** Revert frontend commits; backend unchanged.

## Open Questions

None — UX decisions (separate buttons, detail-only progress, Vitest scope) confirmed during planning.
