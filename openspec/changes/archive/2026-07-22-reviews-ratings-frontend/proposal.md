## Why

Phase 4 backend (`reviews-ratings-backend`) shipped CRUD endpoints for independent episode/season/show reviews, but the frontend has no way to create, edit, or delete them. Users also remove shows from their library without warning, even though removal permanently deletes reviews, watch history, and watch state. This change completes Phase 4.3 and closes the loop between the review API and show-detail UX.

## What Changes

- Add `reviewApi.ts` and `features/reviews/` (TanStack Query hooks, review form components)
- Add shared star components: `StarRatingDisplay` (read-only visual stars) and `StarRatingInput` (interactive half-star, 1.0–5.0); never show raw numeric scores in the UI
- Integrate review forms on show detail at show and season level (always visible) and episode level (collapsible per row)
- Gate "Remove from library" on show detail and library list behind `useConfirm`, warning that reviews, watch history, and watch state will be permanently deleted
- Add Vitest tests for star input, review hooks/components, and removal confirmation

## Capabilities

### New Capabilities

_None — frontend implements existing review and show requirements._

### Modified Capabilities

- `show`: Add requirement that remove-from-library actions in the UI MUST prompt for confirmation before calling the API, with copy warning that reviews, watch history, and watch state for the show will be permanently deleted

## Impact

- **Frontend:** new `api/reviewApi.ts`, `features/reviews/`, `components/StarRatingInput.tsx`, `features/library/components/RemoveFromLibraryButton.tsx`; updates to `ShowDetailPage.tsx` and `LibraryPage.tsx`
- **Backend:** none
- **Dependencies:** none (no new npm packages)
- **Specs:** delta on `show` only; `review` spec unchanged (backend already defines behavior)

## Non-goals

- Batch review endpoint or embedding reviews in show detail response
- Rating roll-up between hierarchy levels
- New star-rating npm library
- Optimistic cache updates
- Analytics or favorites UI (later phases)
