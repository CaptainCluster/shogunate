## Why

Every feature in Shogunate is scoped to an authenticated user. Without registration, login, email verification, password reset, and JWT-based session handling, no other domain (shows, watch tracking, reviews) can be built securely.

## What Changes

- Add Flyway migration for email verification and password reset token tables
- Implement `auth/` package: User entity, repositories, AuthService, AuthController
- Add JWT issuance and validation filter with `CurrentUserResolver`
- Stub email sending for local dev (log tokens to console)
- Add auth REST endpoints under `/api/auth/*`
- Add protected `/api/me` endpoint to verify JWT flow
- Add frontend auth pages (register, login, verify, forgot/reset password)
- Add `authApi.ts`, `useAuth` hook, and route guards
- Add unit and integration tests for auth flows

## Capabilities

### New Capabilities

None — requirements already defined in `openspec/specs/user-auth/spec.md`.

### Modified Capabilities

None — implementing existing spec as-is.

## Non-goals

- OAuth or social login
- Real email delivery in local dev (stub only)
- Show/library or any non-auth domain features
- Refresh tokens (single JWT access token for MVP)

## Impact

- **Backend**: New `auth/` and `common/security/` packages; Spring Security or custom JWT filter; new Gradle deps (security, jjwt or nimbus)
- **Frontend**: New `features/auth/` pages and hooks; protected routes
- **Database**: Two new tables via Flyway V2 migration
