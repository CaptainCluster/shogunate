## Context

Phases 0–3 shipped auth, show library, and watch tracking. [openspec/specs/review/spec.md](../../specs/review/spec.md) defines behavioral requirements; no `review/` implementation or `reviews` table exists yet. Latest Flyway migration is **V7**; this change adds **V8** only.

**Constraint:** Reviews reference shared catalog IDs (`EPISODE`/`SEASON`/`SHOW`) but remain private per user. Library membership must be verified before any review operation (same pattern as `WatchService`).

## Goals / Non-Goals

**Goals:**
- `review/` vertical slice: controller → service → repository → entity/DTOs
- CRUD API scoped via `@CurrentUser` and library membership
- Rating validation: 1.0–5.0 in 0.5 increments (reject 0.5 and values outside range)
- Nullable `updated_at` on insert; Postgres trigger sets it on UPDATE (application never assigns `updatedAt`)
- Wire review cleanup into `ShowService.removeFromLibrary`
- Complete Phase 4 backend tasks 4.1, 4.2, 4.4

**Non-Goals:**
- Frontend UI (Phase 4.3)
- Favorites and analytics (Phases 5–6)
- Enriching show detail response with review fields
- Rating roll-up between levels

## Decisions

### Package layout: new `review/`

**Decision:** Create `com.tvtracker.review/` with `ReviewController`, `ReviewService`, `Review`, `ReviewRepository`, `RatingValidator`, and DTOs.

**Rationale:** Matches ARCHITECTURE §2.1 feature-based vertical slices; mirrors `watch/` pattern.

### Migration V8: `reviews` table + trigger

**Decision:** Add `V8__create_reviews.sql`:

```sql
CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    target_type TEXT NOT NULL CHECK (target_type IN ('EPISODE', 'SEASON', 'SHOW')),
    target_id UUID NOT NULL,
    rating NUMERIC(2,1) NOT NULL,
    body TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    UNIQUE (user_id, target_type, target_id)
);
CREATE INDEX idx_reviews_user_target ON reviews (user_id, target_type, target_id);

CREATE OR REPLACE FUNCTION set_reviews_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_reviews_updated_at
    BEFORE UPDATE ON reviews
    FOR EACH ROW
    EXECUTE FUNCTION set_reviews_updated_at();
```

**Semantics:**
- **Insert:** application sets `created_at` only; `updated_at` remains `NULL`
- **Update:** trigger sets `updated_at = NOW()`; Java must not set `updatedAt` (no `@PreUpdate`)
- **Entity:** `Instant updatedAt` nullable on `Review` and in `ReviewResponse`

**Rationale:** Distinguishes never-edited reviews from edited ones without application-managed timestamps.

**Alternative:** Application-managed `@PreUpdate` — rejected; user requested DB trigger.

### Target resolution and library membership

**Decision:** Copy `resolveShowId` and `verifyLibraryMembership` logic from `WatchService` into `ReviewService` (episode → season → show chain; `userLibraryRepository.existsByUserIdAndShowId`).

**Rationale:** Same scoping rules as watch; keeps review operations tied to library membership.

### API contract

| Method | Path | Success | Notes |
|--------|------|---------|-------|
| POST | `/api/reviews` | 201 + body | Body: `{ targetType, targetId, rating, body? }`; 409 if duplicate |
| PUT | `/api/reviews/{id}` | 200 + body | Update rating/body for own review |
| DELETE | `/api/reviews/{id}` | 204 | Delete own review |
| GET | `/api/reviews?targetType=&targetId=` | 200 + body | Own review for target; 404 if none |

Errors: `401` unauthenticated, `403` not in library, `404` not found / not owner, `409` duplicate create, `400` invalid rating.

**Read path:** Separate GET per target (not embedded in show detail). Frontend treats 404 as "no review yet."

### Rating validation

**Decision:** `RatingValidator` rejects ratings that are not a multiple of 0.5 or fall outside **[1.0, 5.0]** (inclusive). Explicitly reject **0.5**.

**Rationale:** Aligns with PRD §5.4 and [openspec/specs/review/spec.md](../../specs/review/spec.md); corrects TASKS/AGENTS drift toward `[0.5, 5.0]`.

### No roll-up

**Decision:** Each `(userId, targetType, targetId)` is independent. No aggregation, averaging, or derived ratings between levels.

**Rationale:** Non-negotiable per AGENTS.md; favorites Phase 5 reads reviews but does not modify review storage.

### Show integration: review cleanup on library removal

**Decision:** In `ShowService.removeFromLibrary`, after collecting hierarchy `targetIds`, call `reviewRepository.deleteByUserIdAndTargetIdIn(userId, targetIds)` alongside existing watch cleanup.

**Rationale:** Show spec already requires review deletion on remove; stub comment exists at line 153.

### Frontend: library removal confirmation (deferred to `reviews-ratings-frontend`)

**Decision:** Wire `useConfirm` (already used for season/show unmark) into remove-from-library on **both** `ShowDetailPage` and `LibraryPage`. Dialog copy MUST warn that reviews, watch history, and watch state for the show will be permanently deleted. Cancel must not call the API.

**Rationale:** Removal is destructive once reviews exist; no backend `confirm` flag needed — UI-only gate is sufficient. **Spec delta and implementation** belong in `reviews-ratings-frontend` (see TASKS 4.3) so the requirement is not merged before the UI ships.

## Risks / Trade-offs

- **[Risk] JPA stale `updatedAt` after PUT** → Reload entity after save or re-fetch before mapping response so trigger-set timestamp is returned
- **[Risk] Trigger not fired on partial JPA updates** → Ensure PUT persists rating/body via `save()` on a managed entity (always triggers BEFORE UPDATE)
- **[Risk] Cross-user review access** → All queries filter by `userId`; update/delete load by `id AND userId` → 404
- **[Trade-off] N+1 GETs for frontend** → Accepted for MVP; batch endpoint deferred to frontend change if needed

## Migration Plan

1. Add V8 migration on fresh Postgres (`./gradlew flywayMigrate`)
2. Implement `review/` package
3. Wire `ShowService.removeFromLibrary` review cleanup
4. Add tests; verify `./gradlew check`
5. Update `docs/ARCHITECTURE.md` §3 for nullable `updated_at` + trigger

**Rollback:** Revert V8; remove `review/` package; restore `ShowService` stub.

## Open Questions

None — rating range (1.0–5.0), trigger-managed `updated_at`, and separate GET API confirmed during planning.
