## 1. Database Migration

- [x] 1.1 Add Flyway V8 migration `V8__create_reviews.sql`: `reviews` table, index, `set_reviews_updated_at()` function, and `trg_reviews_updated_at` trigger per design.md
- [x] 1.2 Verify V8 runs cleanly on fresh Postgres (`./gradlew flywayMigrate` or app startup)

## 2. Review Package Scaffold

- [x] 2.1 Add `Review` JPA entity (nullable `updatedAt`, no `@PreUpdate`) and `ReviewRepository` with `findByUserIdAndTargetTypeAndTargetId`, `findByIdAndUserId`, `deleteByUserIdAndTargetIdIn`
- [x] 2.2 Add `RatingValidator` (1.0–5.0, 0.5 steps; reject 0.5 and invalid values)
- [x] 2.3 Add DTOs: `CreateReviewRequest`, `UpdateReviewRequest`, `ReviewResponse` with Jakarta validation on create

## 3. ReviewService

- [x] 3.1 Implement target resolution (`resolveShowId`) and library membership check (mirror `WatchService`)
- [x] 3.2 Implement `createReview`: validate rating, reject duplicate with `ConflictException`, set `createdAt` only (leave `updatedAt` null)
- [x] 3.3 Implement `getReviewByTarget`, `updateReview`, `deleteReview`: scope by `userId`; re-fetch after update so trigger-set `updatedAt` is returned

## 4. Review REST API

- [x] 4.1 Add `ReviewController` at `/api/reviews`: POST (201), PUT (200), DELETE (204), GET with query params
- [x] 4.2 Wire `@CurrentUser`, OpenAPI `@Operation` annotations; map errors (400/403/404/409)

## 5. Show Integration

- [x] 5.1 Wire `ShowService.removeFromLibrary` to call `reviewRepository.deleteByUserIdAndTargetIdIn(userId, targetIds)` (replace stub comment)
- [x] 5.2 Update `docs/ARCHITECTURE.md` §3: nullable `updated_at` + trigger note on `reviews`

## 6. Tests

- [x] 6.1 Add `ReviewServiceTest`: valid/invalid ratings, duplicate create, not-owner update/delete
- [x] 6.2 Add `ReviewIntegrationTest`: CRUD HTTP flows, cross-user isolation (404), library membership (403), `updatedAt` null after create and non-null after PUT
- [x] 6.3 Update `ShowIntegrationTest`: remove-from-library deletes reviews for show hierarchy
- [x] 6.4 Verify `./gradlew check` passes including JaCoCo ≥ 80% gate

## 7. Documentation

- [x] 7.1 Verify `/swagger-ui.html` lists all four review endpoints with summaries
- [x] 7.2 On archive: check off Phase 4 tasks 4.1, 4.2, 4.4 in `openspec/TASKS.md`; correct rating range in TASKS/AGENTS to `[1.0, 5.0]`
