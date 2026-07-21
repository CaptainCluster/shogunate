## 1. Database

- [ ] 1.1 Add Flyway V4 migration for shows, seasons, episodes with constraints

## 2. Backend TVmaze & Show Core

- [ ] 2.1 Add TvmazeConfig and TvmazeClient with search and snapshot fetch
- [ ] 2.2 Add Show, Season, Episode entities and repositories
- [ ] 2.3 Implement ShowService (search, add snapshot, list, detail, patch status, delete)
- [ ] 2.4 Implement ShowController REST endpoints

## 3. Backend Tests

- [ ] 3.1 Unit tests for TvmazeMapper and duplicate-add rejection
- [ ] 3.2 Integration tests with mocked TVmaze

## 4. Frontend Library

- [ ] 4.1 Add showApi.ts
- [ ] 4.2 Add search page and library list
- [ ] 4.3 Add show detail page with seasons/episodes
- [ ] 4.4 Add About/Credits section with TVmaze attribution and CC BY-SA notice

## 5. Verification

- [ ] 5.1 Verify ./gradlew test passes
- [ ] 5.2 Verify pnpm build passes
