## Why

Reviews are complete, but users still cannot mark favorite shows or receive rating-based recommendations — a core Phase 5 capability and prerequisite for Phase 6 analytics. This backend change delivers the `favorites` table, show-only favorites CRUD, a separate suggestions API (computed from the user's own SHOW/SEASON reviews), and library-removal cleanup so a follow-up frontend change can wire UI without further backend work.

## What Changes

- Add Flyway **V9**: `favorites` table with unique `(user_id, show_id)` — show-only, no `is_manual`
- Create `favorite/` package: `FavoriteService`, `FavoriteController`, entity/repository, DTOs
- REST: `GET /api/favorites` (user-chosen favorites), `GET /api/favorites/suggestions` (computed show suggestions), `GET /api/favorites/status?showId=`, `POST /api/favorites`, `DELETE /api/favorites/{showId}`
- **BREAKING (spec revision):** Favorites are opt-in only — suggestions never auto-appear in the favorites list; only shows can be favorited (seasons cannot)
- Suggestions derived from per-show weighted scores: show reviews at full rating; season reviews at `rating ÷ season count`
- Wire `ShowService.removeFromLibrary` to delete user's favorite row for the show
- Unit + integration tests; verify `./gradlew check` (JaCoCo ≥ 80% gate)
- Update Phase 5 task **5.1, 5.2, 5.4** acceptance wording in `openspec/TASKS.md` on archive

## Capabilities

### New Capabilities

None — behavioral requirements live in existing `favorite` and `show` specs; this change implements and revises them.

### Modified Capabilities

- `favorite`: show-only favorites; opt-in model (suggestions separate from favorites list); suggestions API with season-weighted scoring from user's own reviews
- `show`: remove-from-library deletes user's favorite row for the show
- `analytics`: favorites reporting references show-only favorites (consistent with revised favorite spec)

## Impact

- **Backend:** new `com.tvtracker.favorite/` package; Flyway V9; `ShowService.removeFromLibrary` cleanup
- **API:** five endpoints under `/api/favorites`
- **Database:** `favorites` table keyed by `(user_id, show_id)`
- **Tests:** `FavoriteServiceTest`, `FavoriteIntegrationTest`; expanded `ShowIntegrationTest`
- **Docs:** `docs/ARCHITECTURE.md` §3 ERD update; `openspec/TASKS.md` Phase 5 checkboxes and acceptance text

## Non-goals

- Frontend favorite toggle, suggestion badge, TanStack hooks (Phase 5.3 — separate `favorites-frontend` change)
- `GET /api/analytics/favorites` dashboard endpoint (Phase 6)
- Dismiss/hide suggestions persistence
- Season-level favorites
- Embedding favorite status in `GET /api/shows/{id}`
- Backend/server-side caching for suggestions (frontend TanStack Query caching is optional in `favorites-frontend`)
