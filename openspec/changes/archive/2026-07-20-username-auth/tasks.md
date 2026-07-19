## 1. Database

- [x] 1.1 Add Flyway V3 migration: drop token tables, replace email columns with username

## 2. Backend Auth

- [x] 2.1 Remove email/token classes (EmailService, token entities/repos, email DTOs)
- [x] 2.2 Refactor User, UserRepository, AuthService, AuthController, JWT, DTOs for username auth
- [x] 2.3 Remove auth token expiration config from application yml files
- [x] 2.4 Rewrite AuthServiceTest and AuthIntegrationTest

## 3. Frontend Auth

- [x] 3.1 Update authApi types and register/login to use username
- [x] 3.2 Update LoginPage, RegisterPage, Layout, HomePage, useAuth hooks
- [x] 3.3 Delete verify/forgot/reset pages, hooks, and routes

## 4. Dev Tooling

- [x] 4.1 Update scripts/populate_test_data.sh for username-based seeding
- [x] 4.2 Update openspec/AGENTS.md dev credentials

## 5. Documentation

- [x] 5.1 Update docs/PRD.md auth sections and User model
- [x] 5.2 Update docs/ARCHITECTURE.md auth package and ER diagram
- [x] 5.3 Update docs/TASKS.md and openspec/TASKS.md Phase 1 auth tasks

## 6. Verification

- [x] 6.1 Run ./gradlew test
- [x] 6.2 Run pnpm build
