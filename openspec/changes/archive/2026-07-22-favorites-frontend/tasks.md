## 1. Search Page Extraction

- [x] 1.1 Create `frontend/src/features/library/SearchPage.tsx` — move TVmaze search form, results, and add-to-library from `LibraryPage`
- [x] 1.2 Add protected route `/search` in `router.tsx`
- [x] 1.3 Add Search nav link in `Layout.tsx`; link from `HomePage` and library empty state to `/search`
- [x] 1.4 Slim `LibraryPage` to library list only (search removed)

## 2. API & Types

- [x] 2.1 Create `frontend/src/api/favoriteApi.ts` with types and five API functions

## 3. Favorites Feature Module

- [x] 3.1 Create `frontend/src/features/favorites/favoriteKeys.ts`
- [x] 3.2 Create hooks: `useFavoriteStatus`, `useFavorites`, `useFavoriteSuggestions`, `useFavoriteMutations`
- [x] 3.3 Create `FavoriteToggle.tsx` and `FavoriteSuggestionsPanel.tsx` + `favorites.css`

## 4. Page Integration

- [x] 4.1 Add `FavoriteToggle` to `ShowDetailPage` header
- [x] 4.2 Add `FavoriteSuggestionsPanel` to `LibraryPage`
- [x] 4.3 Invalidate favorite queries from `useReviewMutations` and `useRemoveShow`
- [x] 4.4 Update `RemoveFromLibraryButton` confirm copy to mention favorites

## 5. Tests

- [x] 5.1 Test `SearchPage` — search submit and results render
- [x] 5.2 Test `useFavoriteMutations` — API calls and query invalidation
- [x] 5.3 Test `useFavoriteStatus` — returns status from API
- [x] 5.4 Test `FavoriteToggle` — badge when suggested; toggle mutations
- [x] 5.5 Test `FavoriteSuggestionsPanel` — renders suggestions; opt-in add

## 6. Verification & Close-out

- [x] 6.1 Run `pnpm lint`, `pnpm build`, and `pnpm test:run`
- [x] 6.2 Manual smoke: search page, suggestions panel, favorite toggle, cross-invalidation
- [x] 6.3 Archive change and check off Phase 5.3 in `openspec/TASKS.md`
