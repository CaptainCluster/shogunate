## 1. Database Migration

- [x] 1.1 Add Flyway V9 migration `V9__create_favorites.sql`: `favorites` table with `(user_id, show_id)` unique, index on `user_id`, per design.md
- [x] 1.2 Verify V9 runs cleanly on fresh Postgres (`./gradlew flywayMigrate` or app startup)

## 2. Favorite Package Scaffold

- [x] 2.1 Add `Favorite` JPA entity and `FavoriteRepository` with `findByUserId`, `findByUserIdAndShowId`, `existsByUserIdAndShowId`, `deleteByUserIdAndShowId`
- [x] 2.2 Add DTOs: `FavoriteResponse`, `FavoriteSuggestionResponse` (`showId`, `weightedScore`), `FavoriteStatusResponse`, `AddFavoriteRequest` with Jakarta validation on `showId`

## 3. FavoriteService

- [x] 3.1 Implement library membership check via `userLibraryRepository.existsByUserIdAndShowId`
- [x] 3.2 Implement `listFavorites(userId)` — all favorite rows for user
- [x] 3.3 Implement `computeSuggestions(userId)` — weighted scores (show review = full rating; season review = rating ÷ season count); per-show max contribution; global max ties; exclude favorited; library-scoped
- [x] 3.4 Implement `getStatus(userId, showId)` — return `{ isFavorite, isSuggested }`
- [x] 3.5 Implement `addFavorite(userId, showId)` — verify library membership, reject duplicate with `ConflictException`
- [x] 3.6 Implement `removeFavorite(userId, showId)` — delete row or `NotFoundException`

## 4. Favorite REST API

- [x] 4.1 Add `FavoriteController` at `/api/favorites`: GET list, GET `/suggestions`, GET `/status?showId=`, POST (201), DELETE `/{showId}` (204)
- [x] 4.2 Wire `@CurrentUser`, OpenAPI `@Operation` annotations; map errors (403/404/409)

## 5. Show Integration

- [x] 5.1 Wire `ShowService.removeFromLibrary` to call `favoriteRepository.deleteByUserIdAndShowId(userId, showId)`
- [x] 5.2 Update `docs/ARCHITECTURE.md` §3: `FAVORITES` table with `show_id` only (remove `is_manual`, season target_type)

## 6. Tests

- [x] 6.1 Add `FavoriteServiceTest`: season weighting (5-season → ÷5, 8-season → ÷8, 1-season → full); show review vs season on same show; global max ties; exclude favorited; cross-user isolation; add/remove
- [x] 6.2 Add `FavoriteIntegrationTest`: favorites vs suggestions separation, POST/DELETE/GET flows, 403 without library membership, 409 duplicate
- [x] 6.3 Update `ShowIntegrationTest`: remove-from-library deletes favorite row for show
- [x] 6.4 Verify `./gradlew check` passes including JaCoCo ≥ 80% gate

## 7. Documentation & Close-out

- [x] 7.1 Verify `/swagger-ui.html` lists all five favorite endpoints with summaries
- [x] 7.2 On archive: check off Phase 5 tasks 5.1, 5.2, 5.4 in `openspec/TASKS.md`; update Phase 5 acceptance wording for show-only opt-in model
