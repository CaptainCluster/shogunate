## 1. Database

- [x] 1.1 Add Flyway V2 migration for email_verification_tokens and password_reset_tokens tables

## 2. Backend Auth Core

- [x] 2.1 Add User entity and UserRepository
- [x] 2.2 Add token entities and repositories
- [x] 2.3 Implement EmailService stub (LoggingEmailService)
- [x] 2.4 Implement AuthService (register, login, verify, resend, forgot, reset)
- [x] 2.5 Implement AuthController with all /api/auth/* endpoints

## 3. Backend Security

- [x] 3.1 Add JwtTokenProvider and jwt config properties
- [x] 3.2 Add JwtAuthenticationFilter and SecurityConfig (public/protected paths)
- [x] 3.3 Add CurrentUser annotation and CurrentUserResolver
- [x] 3.4 Add protected GET /api/me endpoint

## 4. Backend Tests

- [x] 4.1 Unit tests for AuthService (hashing, token expiry, verification gate)
- [x] 4.2 Integration tests for /api/auth/* endpoints with Testcontainers Postgres

## 5. Frontend Auth

- [x] 5.1 Add authApi.ts and api client with Bearer token support
- [x] 5.2 Add useAuth hook and AuthProvider context
- [x] 5.3 Add register, login, verify-email, forgot-password, reset-password pages
- [x] 5.4 Add protected route wrapper and update router

## 6. Verification

- [x] 6.1 Verify ./gradlew test passes
- [x] 6.2 Verify pnpm build passes
