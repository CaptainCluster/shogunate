## 1. Backend Infrastructure

- [x] 1.1 Add docker-compose.yml at repo root with PostgreSQL 16 for local dev
- [x] 1.2 Rename package org.example to com.tvtracker and update Gradle group
- [x] 1.3 Add Gradle dependencies: JPA, PostgreSQL driver, Flyway, springdoc-openapi, Spotless, Checkstyle
- [x] 1.4 Add application.yml and application-local.yml with local profile and DB config
- [x] 1.5 Add common/exception (ApiException, GlobalExceptionHandler) and config/OpenApiConfig
- [x] 1.6 Add Flyway migration V1__create_users_table.sql
- [x] 1.7 Add health/demo controller to verify app boots and Swagger UI loads

## 2. Backend Verification

- [x] 2.1 Verify ./gradlew build succeeds including checkstyle
- [x] 2.2 Verify bootRun connects to Postgres when docker-compose is up

## 3. Frontend Infrastructure

- [x] 3.1 Add react-router-dom and @tanstack/react-query dependencies
- [x] 3.2 Create lib/queryClient.ts and wrap app in QueryClientProvider
- [x] 3.3 Create routes/, components/Layout.tsx, and replace Vite starter with app shell
- [x] 3.4 Add .env.example with VITE_API_BASE_URL

## 4. Frontend Verification

- [x] 4.1 Verify pnpm build succeeds
- [x] 4.2 Verify pnpm dev shows routed shell with layout
