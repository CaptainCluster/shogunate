## Why

Watch tracking is complete, but users still cannot record ratings or written reviews at the episode, season, or show level — a core PRD capability and prerequisite for Phase 5 favorites (auto-suggestion from ratings). Phase 4 backend delivers the `reviews` table, CRUD API, and library-removal cleanup so a separate frontend change can wire review UI without further backend work.

## What Changes

- Add Flyway **V8**: `reviews` table with unique `(user_id, target_type, target_id)`; nullable `updated_at` maintained by a Postgres `BEFORE UPDATE` trigger
- Create `review/` package: `ReviewService`, `ReviewController`, `Review` entity/repository, `RatingValidator`
- REST: `POST`/`PUT`/`DELETE /api/reviews`, `GET /api/reviews?targetType=&targetId=` — ratings validated to **1.0–5.0** in 0.5 steps
- Wire `ShowService.removeFromLibrary` to delete user's reviews for the show hierarchy (stub at line 153)
- Unit + integration tests; verify `./gradlew check` (JaCoCo ≥ 80% gate from Phase 3)
- Check off Phase 4 tasks **4.1, 4.2, 4.4** in `openspec/TASKS.md` on archive; correct rating range in TASKS/AGENTS from `[0.5, 5.0]` → `[1.0, 5.0]`

## Capabilities

### New Capabilities

None — behavioral requirements live in existing `review` and `show` specs; this change implements them.

### Modified Capabilities

- `review`: REST endpoints, rating validation (1.0–5.0, 0.5 steps), library-membership scoping, cross-user isolation, `updated_at` null on create and set on edit via DB trigger
- `show`: remove-from-library deletes user's reviews for the show hierarchy

## Impact

- **Backend:** new `com.tvtracker.review/` package; Flyway V8; `ShowService.removeFromLibrary` cleanup
- **API:** four review endpoints under `/api/reviews`
- **Database:** `reviews` table + trigger function `set_reviews_updated_at`
- **Tests:** `ReviewServiceTest`, `ReviewIntegrationTest`; expanded `ShowIntegrationTest`
- **Docs:** `docs/ARCHITECTURE.md` §3 (nullable `updated_at` + trigger); `openspec/TASKS.md` Phase 4 checkboxes

## Non-goals

- Frontend review UI, star-rating input, TanStack hooks, library-removal confirmation modal (Phase 4.3 — separate `reviews-ratings-frontend` change)
- Embedding review data in `GET /api/shows/{id}` (separate GET per target is sufficient for MVP)
- Favorites auto-suggestion from ratings (Phase 5)
- Analytics over reviews (Phase 6)
- Rating roll-up between episode/season/show levels
