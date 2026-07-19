## Why

Email-based auth added verification and password-reset flows that were never wired to real email delivery (logging stub only). For local MVP use, username + password is simpler and matches how the app is actually tested.

## What Changes

- Replace `email` identity with `username` on the `users` table
- Remove email verification and password reset (token tables, endpoints, EmailService)
- Simplify register/login to username + password; accounts are active immediately after registration
- Update frontend auth pages, API types, and dev seed script
- Update live documentation (PRD, ARCHITECTURE, TASKS, user-auth spec)

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `user-auth`: Registration and login by username; remove email verification and password reset requirements

## Non-goals

- Password reset (any mechanism)
- Email delivery or SMTP
- OAuth or social login
- Refresh tokens
- Auto-login JWT on register (login remains a separate step)

## Impact

- **Breaking API**: register/login bodies use `username`; `/api/me` returns `username` only; four auth endpoints removed
- **Breaking DB**: Flyway V3 drops token tables and email columns; existing local users must re-seed
- **JWT**: `email` claim replaced with `username`
- **Docs**: PRD, ARCHITECTURE, TASKS, AGENTS.md, populate script updated
