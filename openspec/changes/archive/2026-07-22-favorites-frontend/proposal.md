## Why

Phase 5 backend (`favorites-backend`) shipped favorite CRUD, suggestions, and status endpoints, but the frontend has no way to favorite shows or view opt-in suggestions. The library page also mixes TVmaze search with the user's saved shows, which makes both flows harder to scan. This change completes Phase 5.3 and separates search onto its own page.

## What Changes

- Add `favoriteApi.ts` and `features/favorites/` (TanStack Query hooks, toggle, suggestions panel)
- Favorite toggle + suggestion badge on show detail header
- Suggestions panel on library page with opt-in "Add to favorites"
- Extract TVmaze search into dedicated `/search` page; slim library page to suggestions + library list
- Update nav, home links, and remove-from-library confirm copy (mention favorites)
- Invalidate favorite queries when reviews change or a show is removed from library
- Add Vitest tests for search page, favorite hooks, and components

## Capabilities

### New Capabilities

_None — frontend implements existing favorite requirements._

### Modified Capabilities

- `favorite`: Add UI requirements for show-header toggle, suggestion badge, and library suggestions panel
- `show`: Add requirement for dedicated search page; update library removal confirmation copy to mention favorites

## Impact

- **Frontend:** new `api/favoriteApi.ts`, `features/favorites/`, `features/library/SearchPage.tsx`; updates to `LibraryPage`, `ShowDetailPage`, `router.tsx`, `Layout.tsx`, `HomePage.tsx`, review/show hooks, `RemoveFromLibraryButton`
- **Backend:** none
- **Dependencies:** none (no new npm packages)

## Non-goals

- Dedicated `/favorites` list route (Phase 6 analytics may consume favorites)
- Embedding favorite status in `GET /api/shows/{id}`
- Analytics query invalidation (Phase 6)
- Optimistic cache updates
