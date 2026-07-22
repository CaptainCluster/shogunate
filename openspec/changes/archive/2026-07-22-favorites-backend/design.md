## Context

Phases 0–4 shipped auth, show library, watch tracking, and reviews. [`openspec/specs/favorite/spec.md`](../../specs/favorite/spec.md) defines behavioral requirements but no `favorite/` implementation or `favorites` table exists yet. Latest Flyway migration is **V8**; this change adds **V9** only.

**Constraints:**
- Favorites are show-only and always user-chosen (opt-in); suggestions are a separate read-only API
- Suggestions computed exclusively from the requesting user's own SHOW and SEASON reviews
- Library membership must be verified before any favorite mutation (same pattern as `ReviewService`)

## Goals / Non-Goals

**Goals:**
- `favorite/` vertical slice: controller → service → repository → entity/DTOs
- Flyway V9: `favorites` table with unique `(user_id, show_id)`
- REST: list favorites, list suggestions, status by show, add favorite, remove favorite
- Suggestion algorithm: per-show weighted score from SHOW+SEASON reviews (season impact = rating ÷ season count) → global max ties → exclude already-favorited shows
- Wire favorite cleanup into `ShowService.removeFromLibrary`
- Complete Phase 5 backend tasks 5.1, 5.2, 5.4

**Non-Goals:**
- Frontend UI (Phase 5.3 — separate `favorites-frontend` change)
- Analytics dashboard endpoint (Phase 6)
- Dismiss/hide suggestions persistence
- Season-level favorites
- Embedding favorite status in `GET /api/shows/{id}`

## Decisions

### Package layout: new `favorite/`

**Decision:** Create `com.tvtracker.favorite/` with `FavoriteController`, `FavoriteService`, `Favorite`, `FavoriteRepository`, and DTOs.

**Rationale:** Matches ARCHITECTURE §2.1 feature-based vertical slices; mirrors `review/` pattern.

### Migration V9: `favorites` table (show-only)

**Decision:** Add `V9__create_favorites.sql`:

```sql
CREATE TABLE favorites (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    show_id UUID NOT NULL REFERENCES shows (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (user_id, show_id)
);
CREATE INDEX idx_favorites_user ON favorites (user_id);
```

**Rationale:** Show-only favorites; `show_id` FK is clearer than generic `target_type`/`target_id`. No `is_manual` — every row is an explicit user choice.

**Alternative:** Keep `target_type`/`target_id` with CHECK on SHOW — rejected; redundant for show-only scope.

### Suggestion algorithm: season-weighted score → global max ties

**Decision:** In `FavoriteService.computeSuggestions(userId)`:

1. Load user's SHOW and SEASON reviews where parent show is in library
2. For each show, let **N** = number of seasons in the catalog for that show (if N = 0, ignore season reviews for that show)
3. Compute **contributions** toward that show's score:
   - SHOW review with rating **R** → contribution **R** (full impact)
   - SEASON review with rating **R** on a show with **N** seasons → contribution **R / N** (one season's review has 1/N the impact of a show review)
4. **Per-show score** = maximum contribution across that show's show-level and season-level reviews (if none, the show has no score)
5. Find the **global max** score across all scored shows
6. Return shows whose score equals the global max, excluding rows in `favorites` for this user

**Examples:**
- 5-season show, show review 3.0, one season review 5.0 → contributions 3.0 and 1.0 → **score 3.0**
- 5-season show, only season review 5.0 → score **1.0** (5.0 ÷ 5)
- 8-season show, show review 2.0 → score **2.0**
- 1-season show, season review 5.0 → score **5.0** (5.0 ÷ 1; equivalent to a show review)

**Rationale:** A season rating reflects one part of the show; weighting by 1/N prevents a single season review from outweighing a show-level review on the same title, and scales fairly across shows with different season counts.

**Alternative:** Unweighted max of raw ratings — rejected; overweights season reviews relative to show reviews.

**Response field:** `FavoriteSuggestionResponse.weightedScore` (not raw peak rating).

### Separate favorites and suggestions APIs

**Decision:**

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/favorites` | User-chosen favorite shows |
| GET | `/api/favorites/suggestions` | Suggested shows (not yet favorited) |
| GET | `/api/favorites/status?showId=` | `{ isFavorite, isSuggested }` |
| POST | `/api/favorites` | Add favorite `{ showId }` |
| DELETE | `/api/favorites/{showId}` | Remove favorite |

**Rationale:** Opt-in model requires suggestions never merged into favorites list. Status endpoint supports future show-detail UI without embedding in show response.

### Library membership scoping

**Decision:** Verify `userLibraryRepository.existsByUserIdAndShowId(userId, showId)` before add/status; filter suggestions to shows in user's library.

**Rationale:** Same scoping rules as reviews; catalog never exposed without library membership.

### Show integration: favorite cleanup on library removal

**Decision:** In `ShowService.removeFromLibrary`, call `favoriteRepository.deleteByUserIdAndShowId(userId, showId)` after verifying library entry exists.

**Rationale:** Show spec requires favorite cleanup; favorites are show-scoped only (no hierarchy walk needed).

## Risks / Trade-offs

- **[Risk] Suggestion query performance with many reviews** → Acceptable for MVP per-user volume; no backend cache or materialized view
- **[Risk] Cross-user data leak** → All queries filter by `userId` from JWT; integration tests for two users on same show
- **[Risk] Stale suggestions after review change** → Backend always computes fresh; frontend owns caching (see below)
- **[Trade-off] Status requires extra GET** → Accepted for MVP; avoids bloating show detail response

### Frontend caching (optional — `favorites-frontend` change)

**Decision:** No backend caching for suggestions or favorites. The frontend MAY cache suggestion/status/list responses via TanStack Query (`staleTime` / `gcTime` on `useFavoriteSuggestions`, `useFavoriteStatus`, `useFavorites`).

**Invalidation (required when caching is enabled):**
- Favorite add/remove → invalidate `favoriteKeys.list()`, `favoriteKeys.suggestions()`, `favoriteKeys.status(showId)`
- Review create/update/delete → invalidate `favoriteKeys.suggestions()` and affected `favoriteKeys.status(showId)` keys
- Remove from library → invalidate all favorite query keys for that user session

**Rationale:** Per-user data volume is small; TanStack Query avoids redundant network calls on navigation without cross-service cache invalidation on the backend. Default `staleTime: 0` (always refetch on mount) is fine for MVP; a longer `staleTime` (e.g. 1–5 minutes) is optional if mutation invalidation is wired correctly.

**Non-goal:** Redis/Caffeine or other server-side caching for this feature in Phase 5.

## Migration Plan

1. Add V9 migration on fresh Postgres (`./gradlew flywayMigrate`)
2. Implement `favorite/` package
3. Wire `ShowService.removeFromLibrary` favorite cleanup
4. Add tests; verify `./gradlew check`
5. Update `docs/ARCHITECTURE.md` §3 ERD for `favorites` table

**Rollback:** Revert V9; remove `favorite/` package; restore `ShowService` without favorite cleanup.

## Open Questions

None — show-only scope, opt-in favorites, and separate suggestions API confirmed during planning.
