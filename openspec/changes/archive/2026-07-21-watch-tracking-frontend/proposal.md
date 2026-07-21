## Why

Phase 3 backend watch APIs and show-detail watch state are complete, but users still cannot mark or unmark episodes, seasons, or shows from the UI. Phase 3.5 closes the core tracking loop on the show detail page so watch behavior is usable end-to-end before reviews, favorites, or analytics.

## What Changes

- Add `watchApi.ts` and TanStack Query mutation hooks under `features/watch/`
- Extend show detail TypeScript types with `watched` / `watchedAt` from the existing API
- Add mark/unmark controls at show, season, and episode level on `ShowDetailPage` (separate Mark / Unmark buttons per row)
- Add shared `useConfirm` + `ConfirmDialog` for season/show unmark (maps to backend `confirm=true`)
- Show derived season progress on detail only (e.g. `3/10 episodes watched`)
- Set up Vitest + Testing Library; add tests for confirm flow and watch mutations
- Check off Phase **3.5** in `openspec/TASKS.md` on archive

## Capabilities

### New Capabilities

None — backend watch behavior is already specified in `watch` and `show` specs.

### Modified Capabilities

- `watch`: Add frontend UI requirements for mark/unmark controls, cascade confirmation modal, and post-mutation UI refresh on show detail

## Non-goals

- Library list watch progress or summary cards
- Optimistic cache updates (invalidate show detail query on success; matches existing library hook patterns)
- Analytics query invalidation (Phase 6)
- Backend API or schema changes
- Dedicated watch status pages or separate watch GET endpoints

## Impact

- **Frontend**: New `api/watchApi.ts`, `features/watch/` (hooks, components, styles), `hooks/useConfirm.ts`, `components/ConfirmDialog.tsx`; updates to `ShowDetailPage.tsx` and `showApi.ts` types
- **Dependencies**: Vitest, `@testing-library/react`, `@testing-library/user-event`, `@testing-library/jest-dom`, `jsdom` (dev)
- **Unchanged**: Backend, `LibraryPage`, analytics/review/favorite features
