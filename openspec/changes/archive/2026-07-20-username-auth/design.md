## Context

Auth currently uses email + password with verification tokens, password reset tokens, and a logging-only `EmailService`. The frontend has verify/forgot/reset pages wired to those endpoints.

## Goals / Non-Goals

**Goals:**
- Username + password register and login only
- Accounts usable immediately after registration
- JWT session token on login with `username` claim
- Per-user isolation unchanged

**Non-Goals:**
- Password reset
- Email sending
- Refresh tokens

## Decisions

### Username rules
- 3–32 characters
- Pattern: `[a-zA-Z0-9_]+`
- Stored normalized to lowercase; uniqueness checked case-insensitively via `findByUsernameIgnoreCase`

### Registration flow
- `POST /api/auth/register` creates user and returns success message
- No verification gate; user proceeds to login page

### JWT
- Subject: user UUID (unchanged)
- Claim: `username` (replaces `email`)

### Database migration (V3)
Do not edit V1/V2. Add `V3__username_auth.sql`:
1. `DROP TABLE email_verification_tokens, password_reset_tokens`
2. Drop `idx_users_email`, columns `email`, `email_verified`
3. Add `username TEXT NOT NULL UNIQUE` and `idx_users_username`

Existing rows cannot be auto-migrated from email. Local dev: reset Postgres volume and re-run `scripts/populate_test_data.sh`.

### Config cleanup
Remove from `application-local.yml` and `application-test.yml`:
- `auth.verification-token-expiration-hours`
- `auth.password-reset-token-expiration-hours`

## Risks / Trade-offs

- **[Risk] No password recovery** → acceptable per non-goals; users re-register or admin resets DB in local dev
- **[Risk] Breaking change for any existing JWTs/users** → acceptable for pre-production local dev

## Migration Plan

1. V3 Flyway migration
2. Backend auth refactor + tests
3. Frontend auth simplification
4. Dev script and documentation updates
5. `./gradlew test` and `pnpm build`

## Open Questions

None.
