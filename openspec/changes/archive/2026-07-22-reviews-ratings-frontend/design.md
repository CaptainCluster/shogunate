## Context

Phase 4 backend is complete: `ReviewController` exposes CRUD at `/api/reviews`, ratings are validated to 1.0–5.0 in 0.5 steps, and `ShowService.removeFromLibrary` deletes user reviews on show removal. The frontend show detail page ([`ShowDetailPage.tsx`](frontend/src/features/library/ShowDetailPage.tsx)) has watch controls but no review UI. Library removal on show detail and library list fires `DELETE /api/shows/{id}` immediately with no confirmation.

Architecture ([`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) §7.1–7.3) defines the target layout: `api/reviewApi.ts`, `features/reviews/`, shared star component in `components/`, TanStack Query for all server state, and shared `useConfirm` for destructive actions (already used by watch unmark).

## Goals / Non-Goals

**Goals:**
- Wire create/read/update/delete reviews at show, season, and episode level on show detail
- Half-star rating input (1.0–5.0, 0.5 steps) plus optional text body; all rating **display** uses visual stars only — never raw numeric scores in the UI
- Episode-level reviews in collapsible panels to keep long episode lists readable
- Confirmation modal before remove-from-library on show detail and library list, warning about permanent deletion of reviews, watch history, and watch state
- TanStack Query hooks with per-target query keys; Vitest tests for key flows
- Complete Phase 4.3 in `openspec/TASKS.md`

**Non-Goals:**
- Backend changes
- Batch review endpoint or embedding reviews in `GET /api/shows/{id}`
- Rating roll-up between levels
- New npm star-rating library
- Optimistic cache updates
- Analytics query invalidation (Phase 6)

## Decisions

### API layer: `reviewApi.ts` mirrors backend endpoints

**Decision:** Four typed functions wrapping `POST`/`GET`/`PUT`/`DELETE` on `/api/reviews`. `GET` returns a review or throws `ApiError` with status 404.

**Rationale:** Matches existing `watchApi.ts` / `showApi.ts` pattern; pages never call `fetch` directly.

### Server state: per-target queries, 404 as empty form

**Decision:** `useReview(targetType, targetId)` uses `reviewKeys.target(type, id)`. The query function catches 404 and returns `null` (no review yet). Mutations invalidate only the affected target key.

**Rationale:** Backend serves one review per target via separate GET; show detail does not embed review data. N+1 GETs on page load are accepted for MVP (same trade-off noted in backend design).

**Alternative:** Batch endpoint — rejected; out of scope for this change.

### Star display and input: `StarRatingDisplay` + `StarRatingInput` in `components/`

**Decision:** Two shared components sharing the same star glyph rendering (filled, half-filled, empty star icons for each of 5 positions):

- **`StarRatingDisplay`** — read-only; renders visual stars for a numeric rating. Used anywhere a score is shown without editing (e.g. episode collapsible summary). Never renders the numeric value as visible text.
- **`StarRatingInput`** — interactive; same visual stars, each split into left/right clickable halves (values 1.0, 1.5, …, 5.0). Keyboard support (arrow keys step by 0.5) and slider ARIA attributes; `aria-valuenow` exposes the numeric value to assistive tech only.

**Rationale:** User requirement — stars, not numbers, in the UI. Splitting display vs input keeps summaries lightweight and avoids duplicating half-star CSS. Architecture §7.1 lists "stars" in `components/`; no npm dependency.

### Review form: `ReviewEditor` + `ReviewCollapsible`

**Decision:**
- `ReviewEditor` — core form (stars, textarea, Save, Delete) used at show and season level (always visible)
- `ReviewCollapsible` — native `<details>` / `<summary>` wrapper for episode rows; collapsed by default; summary shows `Review` label plus `StarRatingDisplay` when a review exists (no numeric score text)

**Rationale:** User preference from planning — episode rows already have watch controls; always-inline forms would clutter shows with many episodes.

**Form behavior:**
- No review (404): empty form, Save → POST
- Existing review: pre-filled, Save → PUT, Delete review → DELETE
- Local `useState` for draft fields; disable Save until rating is valid

### Library removal: shared `RemoveFromLibraryButton`

**Decision:** Extract a component wrapping `useRemoveShow()` + `useConfirm()`. Used on both `ShowDetailPage` and `LibraryPage`.

**Copy:**
- Title: "Remove from library?"
- Message: `Remove "{title}" from your library? This will permanently delete your reviews, watch history, and watch state for this show.`
- Confirm label: "Remove"

Cancel or dismiss → no API call. Confirm → existing `DELETE /api/shows/{id}` mutation.

**Rationale:** Matches `WatchButtonPair` confirm-before-mutate pattern; backend design deferred this UI gate to this change.

### Testing: Vitest + RTL, mocked API

**Decision:** Test `StarRatingDisplay`, `StarRatingInput`, `useReview` (404 handling), `useReviewMutations`, `ReviewEditor`, `ReviewCollapsible`, and `RemoveFromLibraryButton`. Mock `reviewApi` / `showApi` — no live backend in unit tests. Assert summaries and read-only views render star icons, not numeric text.

**Rationale:** Matches watch-tracking-frontend test scope; Architecture §8.3.

## Risks / Trade-offs

- **[Risk] N+1 review GETs on large shows** → Accept for MVP; collapsible panels limit visual clutter even if queries remain per-target
- **[Risk] Invalidation flicker after save** → Accept for MVP; optimistic updates deferred
- **[Risk] Half-star click targets too small on mobile** → Use adequate touch targets in CSS; test in manual smoke
- **[Trade-off] Reviews not visible on library list** → Users open show detail to review; acceptable per scope

## Migration Plan

1. Add `reviewApi.ts`, `features/reviews/` hooks and components, `StarRatingInput`
2. Add `RemoveFromLibraryButton`; update `ShowDetailPage` and `LibraryPage`
3. Add Vitest tests
4. Verify: `pnpm lint`, `pnpm build`, `pnpm test:run`; manual smoke on show detail
5. Archive change; check off TASKS.md 4.3

**Rollback:** Revert frontend commits; backend unchanged.

## Open Questions

None — episode collapsible layout, visual-only star display, and custom star components confirmed during planning.
