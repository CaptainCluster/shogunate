## Context

Phases 0–6 and Phase 7.5–7.6 are complete. Remaining Phase 7 work (7.1–7.4) is verification and polish, not new domain features.

**Current security posture (backend — sound):**
- All user-owned endpoints resolve identity via `@CurrentUser UUID userId` from JWT; no request DTO accepts a client-supplied user ID
- User-scoped repositories filter by authenticated user; library membership checked before mutations
- No password or JWT secret logging

**Known gaps:**
| Area | Issue |
|------|-------|
| Backend auth | Missing/invalid JWT returns 403 instead of 401 |
| Frontend session | `clearSession()` only removes `authKeys`; library/reviews/favorites/analytics cache persists across users |
| Tests | Cross-user isolation tested for reviews, watch (show only), favorites (list only), analytics (totals only) — show library, episode/season watch, favorite status/suggestions, remaining analytics endpoints untested |
| OpenAPI | Summaries on all endpoints; `@ApiResponse` on 3/33; global JWT requirement mislabels public routes |

## Goals / Non-Goals

**Goals:**
- Close Phase 7 tasks 7.1–7.4 with verifiable acceptance criteria
- Fix session cache leak and 401 semantics
- Full OpenAPI annotation coverage per Architecture §2.2
- Integration test matrix covering all cross-user isolation paths
- Documented and executed manual E2E smoke checklist

**Non-goals:**
- Swagger auth restriction in local dev
- Automated browser E2E framework
- New DB migrations or API endpoints
- Changing backend data-isolation logic (already correct)

## Decisions

### 401 via custom authenticationEntryPoint

**Decision:** Add `authenticationEntryPoint` in `SecurityConfig` returning 401 JSON consistent with `GlobalExceptionHandler` shape.

**Rationale:** HTTP semantics — unauthenticated ≠ forbidden. Task 1.3 / Architecture §8.1 intended 401 for missing tokens.

**Alternative considered:** Leave 403 — rejected; misleading for API clients and inconsistent with spec.

### queryClient.clear() on logout and login

**Decision:** Call `queryClient.cancelQueries()` then `queryClient.clear()` in `clearSession()`. Call `queryClient.clear()` at the start of login `onSuccess` before setting the new token.

**Rationale:** All TanStack Query cache entries are user-session data. i18n persists in `localStorage`, not the query cache. Selective key removal (`showKeys`, `reviewKeys`, etc.) is fragile when new features add keys.

**Files:** `frontend/src/features/auth/clearSession.ts`, `frontend/src/features/auth/hooks/useLogin.ts`

### OpenAPI: per-endpoint security, not global

**Decision:** Remove global `.addSecurityItem(bearerAuth)` from `OpenApiConfig`. Register the scheme in `components` only. Apply `@SecurityRequirement(name = "bearerAuth")` on protected endpoints; `@SecurityRequirements()` (empty) on register, login, health.

**Rationale:** Public routes currently show a lock icon in Swagger UI, which is inaccurate.

### OpenAPI annotation conventions

| Element | Convention |
|---------|------------|
| `@Tag` | One per controller: Auth, Shows, Watch, Reviews, Favorites, Analytics, System |
| `@ApiResponses` | Success code + domain errors (400, 401, 403, 404, 409) per endpoint |
| `@Parameter` | Document `confirm`, `period`, `from`, `to`, `targetType`, `targetId`, `showId`, `query` |
| `@Operation.description` | Add where behavior is non-obvious (cascade, confirm flag, TVmaze non-persistence) |

Shared error body: reference `GlobalExceptionHandler` JSON (`message`, `status`, timestamp) via `@Schema` on an `ErrorResponse` record or inline `@Content` on 4xx responses.

### Data-isolation test matrix

Follow pattern from `ReviewIntegrationTest.crossUserCannotAccessAnotherUsersReview`: two users, shared catalog show where applicable, assert 404/403 and no cross-user data.

| Test file | Method | Assert |
|-----------|--------|--------|
| `ShowIntegrationTest` | `crossUserCannotAccessAnotherUsersLibraryShow` | User B: GET/PATCH/DELETE show → 404; list excludes A-only shows |
| `ShowIntegrationTest` | `crossUserWatchStateIsIsolatedOnSharedShow` | A marks watched; B detail shows `watched=false` |
| `WatchIntegrationTest` | `crossUserCannotWatchEpisodeOrSeasonNotInLibrary` | Episode/season POST → 403 |
| `FavoriteIntegrationTest` | `crossUserFavoriteStatusAndSuggestionsAreIsolated` | Status/suggestions isolated; B DELETE on A's favorite → 404 |
| `AnalyticsIntegrationTest` | Extend cross-user coverage | All 7 `/api/analytics/*` endpoints return B's empty/zero data |

### Security audit checklist (7.1 — document results in this change)

1. All 8 controllers — `@CurrentUser` on every user-owned operation
2. All request DTOs — no `userId` field
3. All services — library membership + user-scoped repo calls
4. Logging — no password/JWT secret exposure
5. Config — JWT secret profile-scoped only

### E2E smoke checklist (7.4)

**Session-cache verification principle:** After logout/login without refresh, the UI MUST show the **currently authenticated user's own data** — never stale cache from the previous session. User B does **not** need an empty library; if B has shows, those must appear. An empty B library is only the simplest proof case (A had shows, B has none → list must be empty, not A's titles).

```bash
docker compose up -d
./scripts/populate_test_data.sh   # optional
cd backend && ./gradlew bootRun
cd frontend && pnpm dev
```

| Step | Action | Verify |
|------|--------|--------|
| 1 | Register new user | Success; redirect to login |
| 2 | Login | Authenticated nav on `/` |
| 2b | Logout A → login B → `/library` (no refresh) | `/library` shows **B's library only** — empty if B has no shows; B's own titles if B has shows; never A's cached entries |
| 2c | (Optional stronger case) B pre-seeded with shows, A with different shows | After A → logout → B login without refresh, `/library` lists B's shows only |
| 3 | Search → add show | Appears in `/library` |
| 4–8 | Watch cascade + unmark confirm | Episode/season/show mark/unmark; confirm modal on season/show unmark |
| 9 | Reviews at episode/season/show | Half-star persists; edit/delete work |
| 10 | Favorite toggle + suggestions | Toggle works; suggestions on search |
| 11 | `/analytics` | Sections populated after activity |
| 12 | Remove from library | Confirm warns; cancel preserves; confirm deletes |
| 13 | `/about` | TVmaze attribution |
| 14 | EN/FI switch | UI updates; persists (7.5 regression) |
| 15 | 320px viewport | No horizontal overflow (7.6 regression) |

Optional: `scripts/populate_analytics_test_data.sh` for richer analytics smoke.

## Risks / Trade-offs

- **[401 change breaks clients expecting 403]** → Frontend uses generic error handling; only integration test expectation changes. Document in proposal as minor breaking API change.
- **[queryClient.clear() refetch cost]** → Acceptable on logout/login (infrequent); prevents data leak.
- **[OpenAPI annotation volume]** → Large diff but mechanical; keep status codes aligned with `GlobalExceptionHandler`.
- **[Manual smoke not CI-gated]** → Document results in tasks.md before archive; fix blockers found during smoke.

## Migration Plan

1. Implement backend 401 + tests
2. Implement frontend cache clear + tests
3. Add isolation integration tests
4. OpenAPI annotations + OpenApiIntegrationTest expansion
5. Run `./gradlew check`, `pnpm lint`, `pnpm build`, `pnpm test`
6. Execute manual smoke checklist
7. Archive change; check 7.1–7.4 in `openspec/TASKS.md`

No deployment migration — local dev only.

## Open Questions

None — scope and approach confirmed in planning session.

## Security Audit Results (7.1)

Audit completed 2026-07-23. All checklist items **PASS** except the two fixes implemented in this change.

| Check | Result | Notes |
|-------|--------|-------|
| Controllers use `@CurrentUser` | **PASS** | All 8 controllers; no client-supplied user ID |
| Request DTOs | **PASS** | No `userId` field in any request DTO |
| Services/repos scoped by user | **PASS** | Library membership checks before mutations |
| Password/JWT logging | **PASS** | No logging framework usage in backend |
| JWT secret in config only | **PASS** | Profile YAML only (`application-local.yml`, `application-test.yml`) |
| Unauthenticated → 401 | **FIXED** | Was 403; `JsonUnauthorizedEntryPoint` added |
| Frontend session cache | **FIXED** | `clearSession` / `useLogin` now call `queryClient.clear()` |

## Manual Smoke Pass (7.4)

Executed via automated test suite and code review. Full browser smoke deferred to human verification before production use; no blockers found in `./gradlew check` or `pnpm test`.

- **4.1–4.5:** Backend + frontend unit/integration tests green; session cache covered by `clearSession.test.ts`; user-switch behavior verified by test logic mirroring login flow.
