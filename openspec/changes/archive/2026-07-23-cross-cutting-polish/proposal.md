## Why

Phase 7 tasks **7.1–7.4** remain open after i18n (7.5) and UI refinement (7.6). The backend enforces per-user isolation correctly, but we have not formally verified security, filled isolation test gaps, brought OpenAPI docs up to Architecture §2.2, or run a full end-to-end smoke pass. A frontend cache bug also lets User B see User A's library after logout/login until refresh — a client-side data leak that must be fixed before calling Phase 7 complete.

## What Changes

- **7.1 Security review:** Document audit checklist results; return **401 Unauthorized** (not 403) for missing/invalid JWT on protected routes; clear TanStack Query cache on logout and login so user-scoped data cannot bleed across sessions
- **7.2 Data-isolation audit:** Add integration tests for show library, watch episode/season, favorites status/suggestions, and all analytics endpoints — gaps not covered by existing cross-user tests
- **7.3 OpenAPI review:** Remove misleading global JWT requirement; add `@Tag`, `@ApiResponses`, `@Parameter`, and per-endpoint `@SecurityRequirement` on all 33 endpoints; expand `OpenApiIntegrationTest` to cover every path
- **7.4 E2E smoke pass:** Execute documented full-flow checklist (register → analytics) including user-switch cache verification; fix blockers before archive
- Check off Phase 7 tasks **7.1–7.4** in `openspec/TASKS.md` on archive

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `user-auth`: Unauthenticated protected API requests return 401; frontend MUST clear all cached server state on logout and before loading data for a new login session

## Impact

- **Backend:** `SecurityConfig` authentication entry point; `@ApiResponse` / `@Tag` annotations across 7 controllers; `OpenApiConfig`; new/extended integration tests in show, watch, favorite, analytics packages; `OpenApiIntegrationTest` expanded to 33 paths
- **Frontend:** `clearSession.ts`, `useLogin.ts`; new `clearSession.test.ts`
- **API:** **BREAKING** (minor) — unauthenticated requests to protected endpoints change from HTTP 403 to 401
- **Docs:** Phase 7.1–7.4 checked in `openspec/TASKS.md`

## Non-goals

- Restricting Swagger UI in local dev
- New product features or schema migrations
- Automated Playwright/Cypress E2E suite (smoke remains manual per TASKS.md)
- Phase 7.5 / 7.6 regression work beyond smoke checklist items
