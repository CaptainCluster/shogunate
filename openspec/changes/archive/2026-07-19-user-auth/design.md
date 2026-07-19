## Context

Foundation setup provides Postgres, Flyway, `users` table, OpenAPI, and frontend shell. The user-auth spec defines registration, verification, login, password reset, and per-user isolation. Architecture specifies JWT Bearer tokens and bcrypt password hashing.

## Goals / Non-Goals

**Goals:**
- Full auth API matching `openspec/specs/user-auth/spec.md`
- JWT required on protected endpoints; current user resolved from token only
- Email verification and password reset with time-limited single-use tokens
- Frontend E2E flow: register → verify → login → access protected route

**Non-Goals:**
- Spring Security OAuth2 resource server complexity beyond JWT filter
- Real SMTP in local dev
- Refresh token rotation

## Decisions

### Password hashing: BCrypt via Spring Security Crypto
Use `BCryptPasswordEncoder` without full Spring Security filter chain complexity — custom JWT filter for stateless auth.

### JWT library: jjwt 0.12.x
Industry standard, works with Java 21. Secret from `application-local.yml`.

### Token storage
- `email_verification_tokens`: token (UUID), user_id, expires_at, used_at
- `password_reset_tokens`: same shape

### Email stub
`EmailService` interface with `LoggingEmailService` implementation that logs verification/reset links to SLF4J.

### JWT filter
`JwtAuthenticationFilter` extends OncePerRequestFilter; sets `SecurityContext` or custom `@CurrentUser` argument resolver reading from request attribute.

### Public vs protected paths
Public: `/api/auth/**`, `/api/health`, `/swagger-ui/**`, `/v3/api-docs/**`
Protected: everything else under `/api/**`

### Frontend token storage
`localStorage` key `auth_token`; `useAuth` provides user state; axios/fetch wrapper adds Bearer header.

## Risks / Trade-offs

- **[Risk] JWT in localStorage** → XSS exposure; acceptable for MVP local dev
- **[Risk] No refresh tokens** → user re-logs on expiry; acceptable per non-goals

## Migration Plan

1. V2 migration for token tables
2. Backend auth implementation + tests
3. Frontend pages wired to API
4. Manual E2E verification

## Open Questions

None.
