## 1. API & Types

- [x] 1.1 Create `frontend/src/api/reviewApi.ts` with `Review`, `ReviewTargetType`, and CRUD functions (`getReview`, `createReview`, `updateReview`, `deleteReview`)

## 2. Review Feature Module

- [x] 2.1 Create `frontend/src/features/reviews/reviewKeys.ts` with `target(type, id)` query keys
- [x] 2.2 Create `frontend/src/features/reviews/hooks/useReview.ts` — TanStack query; 404 → `null`
- [x] 2.3 Create `frontend/src/features/reviews/hooks/useReviewMutations.ts` — create/update/delete; invalidate target key on success
- [x] 2.4 Create `frontend/src/components/StarRatingDisplay.tsx` — read-only filled/half/empty star icons; no visible numeric text
- [x] 2.4b Create `frontend/src/components/StarRatingInput.tsx` — interactive half-star input (1.0–5.0), reuses star glyph styling, keyboard + ARIA
- [x] 2.5 Create `frontend/src/features/reviews/components/ReviewEditor.tsx` — stars, textarea, Save, Delete review
- [x] 2.6 Create `frontend/src/features/reviews/components/ReviewCollapsible.tsx` — `<details>` wrapper; summary uses `StarRatingDisplay` when review exists (stars only, no numeric score)
- [x] 2.7 Add `frontend/src/features/reviews/reviews.css` — form and collapsible styling

## 3. Library Removal Confirmation

- [x] 3.1 Create `frontend/src/features/library/components/RemoveFromLibraryButton.tsx` — `useConfirm` gate with destructive-data warning
- [x] 3.2 Replace direct remove calls in `ShowDetailPage.tsx` with `RemoveFromLibraryButton`
- [x] 3.3 Replace direct remove calls in `LibraryPage.tsx` with `RemoveFromLibraryButton`

## 4. Show Detail Integration

- [x] 4.1 Add show-level `ReviewEditor` to `ShowDetailPage.tsx` header
- [x] 4.2 Add season-level `ReviewEditor` to each season block
- [x] 4.3 Add episode-level `ReviewCollapsible` to each episode row
- [x] 4.4 Surface review mutation errors inline via `getErrorMessage`

## 5. Tests

- [x] 5.1 Test `StarRatingDisplay` — correct filled/half/empty stars for sample ratings; no numeric text in DOM
- [x] 5.1b Test `StarRatingInput` — half-star clicks and keyboard step
- [x] 5.2 Test `useReview` — 404 returns null; 200 returns review
- [x] 5.3 Test `useReviewMutations` — correct API calls and query invalidation
- [x] 5.4 Test `ReviewEditor` — create (POST), update (PUT), delete (DELETE)
- [x] 5.5 Test `ReviewCollapsible` — collapsed by default; summary shows `StarRatingDisplay` (not numeric text) when review exists
- [x] 5.6 Test `RemoveFromLibraryButton` — cancel sends no request; confirm calls mutate

## 6. Verification & Close-out

- [x] 6.1 Run `pnpm lint`, `pnpm build`, and `pnpm test:run`
- [x] 6.2 Manual smoke: create/edit/delete reviews at show, season, episode level; half-star persists; remove confirm/cancel on detail and list
- [x] 6.3 Archive change and check off Phase 4.3 in `openspec/TASKS.md`
