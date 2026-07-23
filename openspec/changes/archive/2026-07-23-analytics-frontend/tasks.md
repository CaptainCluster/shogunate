## 1. API & Types

- [x] 1.1 Create `frontend/src/api/analyticsApi.ts` with types and seven GET functions matching backend DTOs
- [x] 1.2 Create `frontend/src/features/analytics/analyticsKeys.ts` with namespaced query keys

## 2. Hooks & Utilities

- [x] 2.1 Create analytics hooks: `useAnalyticsTotals`, `useWatchCounts`, `useLongestToWatch`, `useAnalyticsFavorites`, `useWatchStreaks`, `useLibraryCompletion`, `usePlanToWatchCount`
- [x] 2.2 Create `formatDuration`, `formatPercent`, `formatUtcDate` utilities with unit tests for `formatDuration`

## 3. Dashboard Components

- [x] 3.1 Create `TotalsSection`, `WatchCountsSection` (period toggle + date inputs + CSS bar chart), `LongestToWatchSection`
- [x] 3.2 Create `FavoritesSection` (join with library for titles), `WatchStreaksSection`, `LibraryCompletionSection`, `PlanToWatchSection`
- [x] 3.3 Create `AnalyticsPage.tsx` composing all sections + `analytics.css`

## 4. Routing & Navigation

- [x] 4.1 Add protected `/analytics` route in `router.tsx`
- [x] 4.2 Add Analytics nav link in `Layout.tsx` and link from `HomePage.tsx`

## 5. Query Invalidation

- [x] 5.1 Export `invalidateAllAnalyticsQueries` from analytics feature module
- [x] 5.2 Wire invalidation into `useWatchMutations`, `useFavoriteMutations`, and `useShowLibrary` mutations (add/remove/status)

## 6. Tests

- [x] 6.1 Test `WatchCountsSection` — period toggle and bar rendering
- [x] 6.2 Test `LongestToWatchSection` — ranked list and empty state
- [x] 6.3 Test `FavoritesSection` — library join for titles
- [x] 6.4 Extend `useWatchMutations.test.tsx` — invalidates `analyticsKeys.all` on success

## 7. Verification & Close-out

- [x] 7.1 Run `pnpm lint`, `pnpm build`, and `pnpm test:run`
- [x] 7.2 Manual smoke with `scripts/populate_analytics_test_data.sh` — all sections populated; watch/favorite mutations refresh dashboard
- [x] 7.3 Archive change and check off Phase 6.4 in `openspec/TASKS.md`
