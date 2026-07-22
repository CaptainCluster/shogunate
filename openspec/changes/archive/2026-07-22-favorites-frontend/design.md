## Context

Phase 5 backend is complete: `FavoriteController` exposes list, suggestions, status, add, and remove at `/api/favorites`. The frontend has auth, library, watch, and review UI but zero favorite integration. [`LibraryPage.tsx`](frontend/src/features/library/LibraryPage.tsx) currently combines TVmaze search with the user's library list.

Architecture ([`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) §7.1–7.3) defines `api/favoriteApi.ts`, `features/favorites/`, TanStack Query for server state, and cross-feature cache invalidation on mutations.

## Goals / Non-Goals

**Goals:**
- Favorite toggle on show detail header with suggestion badge when `isSuggested && !isFavorite`
- Suggestions panel on library page; opt-in add via POST (never auto-favorite)
- Dedicated `/search` page for TVmaze search + add-to-library
- TanStack Query hooks with `favoriteKeys`; invalidate on favorite/review/library mutations
- Vitest tests for key flows
- Complete Phase 5.3 in `openspec/TASKS.md`

**Non-Goals:**
- Backend changes
- `/favorites` route or analytics invalidation
- Embedding favorite status in show detail API response
- Optimistic cache updates

## Decisions

### API layer: `favoriteApi.ts` mirrors backend endpoints

**Decision:** Five typed functions wrapping GET list/suggestions/status, POST add, DELETE remove.

**Rationale:** Matches `reviewApi.ts` / `watchApi.ts` pattern.

### Server state: `favoriteKeys` namespace

**Decision:** Hooks in `features/favorites/hooks/`:

- `useFavoriteStatus(showId)` — status for show detail toggle/badge
- `useFavoriteSuggestions()` — suggestions for library panel
- `useFavorites()` — list (optional; panel joins with library metadata)
- `useFavoriteMutations(showId)` — add/remove with invalidation of `list`, `suggestions`, `status(showId)`

**Cross-feature invalidation:**

- `useReviewMutations` → invalidate all `favoriteKeys.all` on success
- `useRemoveShow` → invalidate all `favoriteKeys.all` on success

**Rationale:** Suggestions depend on review scores; library removal deletes favorites server-side.

### Components

**`FavoriteToggle`** — show detail header:

- Shows "Add to favorites" / "Remove from favorites" based on `isFavorite`
- Badge "Suggested favorite" when `isSuggested && !isFavorite`
- No confirmation modal (non-destructive)

**`FavoriteSuggestionsPanel`** — library page:

- Joins suggestion `showId`s with `useShowLibrary()` metadata
- Each row: title link, "Add to favorites" button
- Empty/hidden when no suggestions

**`SearchPage`** — `/search`:

- Extract existing search form + results from `LibraryPage`
- Reuse `useShowSearch`, `useAddShow`, `LibraryPage.css` classes

### Page layout after change

| Route | Content |
|-------|---------|
| `/search` | TVmaze search + add to library |
| `/library` | Suggestions panel + library list |
| `/library/:id` | Show detail + `FavoriteToggle` |

Nav: Home | Library | Search | About (Search visible when authenticated)

### Library removal copy

**Decision:** Extend confirm message to mention favorites deletion alongside reviews, watch history, and watch state.

## Risks / Trade-offs

- **[Risk] Suggestion panel needs library join for titles** → Join client-side from `useShowLibrary()`; suggestions only include library shows
- **[Risk] Stale suggestions after review edit** → Invalidate favorite queries from review mutations
- **[Trade-off] Extra GET for status on show detail** → Accepted; backend deferred embedding in show response

## Migration Plan

1. Add OpenSpec artifacts; implement search page extraction
2. Add `favoriteApi.ts`, hooks, components, CSS
3. Wire pages; cross-invalidation; update confirm copy
4. Tests; `pnpm lint`, `pnpm build`, `pnpm test:run`
5. Archive change; check off TASKS.md 5.3

**Rollback:** Revert frontend commits; backend unchanged.

## Open Questions

None — search on dedicated page and library suggestions panel confirmed during planning.
