## Context

Phase 6 backend (`analytics-backend`) is complete: seven GET endpoints under `/api/analytics` expose totals, watch counts by period, longest-to-watch ranking, favorites, watch streaks, library completion, and plan-to-watch count. The frontend has auth, library, watch, review, and favorite UI but no analytics integration. [`openspec/TASKS.md`](../../TASKS.md) task **6.4** remains unchecked.

Architecture ([`docs/ARCHITECTURE.md`](../../../docs/ARCHITECTURE.md) §7.1–7.3) defines `api/analyticsApi.ts`, `features/analytics/`, TanStack Query for server state, and cross-feature cache invalidation on mutations that affect analytics source data.

## Goals / Non-Goals

**Goals:**
- Protected `/analytics` dashboard consuming all seven backend endpoints
- TanStack Query hooks with `analyticsKeys`; invalidate on watch/favorite/library mutations
- Period selector for watch counts (MONTH / YEAR / CUSTOM)
- CSS bar charts for watch counts and completion (no new chart npm dependency)
- UTC date/time display with explicit labels
- Vitest tests for formatters, key sections, and invalidation
- Complete Phase 6.4 in `openspec/TASKS.md`

**Non-Goals:**
- Backend changes
- Streak history calendar / heatmap
- Plan-to-watch show list (count stat only)
- Local-time date conversion
- Embedding analytics summary on home page (link only)

## Decisions

### API layer: `analyticsApi.ts` mirrors backend endpoints

**Decision:** Seven typed GET functions wrapping `/api/analytics/*`.

**Rationale:** Matches `favoriteApi.ts` / `reviewApi.ts` pattern.

### Server state: `analyticsKeys` namespace

**Decision:** Hooks in `features/analytics/hooks/`:

| Hook | Query key | Endpoint |
|------|-----------|----------|
| `useAnalyticsTotals()` | `totals` | `/api/analytics/totals` |
| `useWatchCounts(period, from, to?)` | `watchCounts(period, from, to)` | `/api/analytics/watch-counts` |
| `useLongestToWatch()` | `longestToWatch` | `/api/analytics/longest-to-watch` |
| `useAnalyticsFavorites()` | `favorites` | `/api/analytics/favorites` |
| `useWatchStreaks()` | `watchStreaks` | `/api/analytics/watch-streaks` |
| `useLibraryCompletion()` | `libraryCompletion` | `/api/analytics/library-completion` |
| `usePlanToWatchCount()` | `planToWatchCount` | `/api/analytics/plan-to-watch-count` |

**Cross-feature invalidation** via `invalidateAllAnalyticsQueries(queryClient)`:

- `useWatchMutations` — watch_events, streaks, completion
- `useFavoriteMutations` — analytics favorites
- `useRemoveShow`, `useAddShow`, `useUpdateLibraryStatus` — library-scoped metrics

**Rationale:** Architecture §7.2 requires analytics invalidation on cascade mutations.

### Dashboard layout: single page, seven sections

**Decision:** `AnalyticsPage.tsx` at `/analytics` with sections (top → bottom):

1. Totals — stat cards (episodes / seasons / shows)
2. Watch counts by period — period toggle + date inputs + CSS bar chart
3. Longest to watch — ranked list with links to show detail
4. Favorites — list joined with library for titles/posters
5. Watch streaks — current + longest streak stat cards
6. Library completion — overall % + per-show progress bars
7. Plan to watch — count stat

**Default watch-counts state:** `period=MONTH`, `from=today` (current calendar month).

**Rationale:** Backend built all seven endpoints for dashboard consumption; single page keeps navigation simple.

### Charts: CSS bars, no npm dependency

**Decision:** Horizontal CSS bars scaled relative to max count, using existing `--accent` CSS variables from `index.css`.

**Alternatives considered:** Recharts — rejected to match favorites-frontend no-new-deps approach and keep bundle small.

### Favorites titles: client-side join

**Decision:** Join `useAnalyticsFavorites()` with `useShowLibrary()` to resolve `showId` → title/poster.

**Rationale:** Backend non-goal to enrich `/api/analytics/favorites` with show metadata.

### Date display: UTC with labels

**Decision:** Format all analytics dates/times in UTC with an explicit "UTC" suffix via `formatUtcDate()`.

**Rationale:** Backend streak and period logic uses UTC; avoids mismatch with local conversion.

### Page layout after change

| Route | Content |
|-------|---------|
| `/analytics` | Full analytics dashboard (seven sections) |

Nav: Home | Library | Search | **Analytics** | About (Analytics visible when authenticated)

## Risks / Trade-offs

- **[Risk] Favorites section needs library join for titles** → Join client-side from `useShowLibrary()`; orphaned favorites (show removed) show showId fallback
- **[Risk] Stale analytics after watch/favorite/library mutations** → Invalidate all `analyticsKeys` from relevant mutation hooks
- **[Risk] Watch-counts period UX confusion** → Echo resolved `from`/`to` from API response; label CUSTOM requires both dates
- **[Trade-off] CSS bars vs interactive charts** → Sufficient for MVP counts; no zoom/tooltip library

## Migration Plan

1. Add OpenSpec artifacts; implement `analyticsApi.ts`, hooks, formatters
2. Build dashboard page + section components + CSS
3. Wire route, nav, home link; cross-invalidation
4. Tests; `pnpm lint`, `pnpm build`, `pnpm test:run`
5. Manual smoke with `scripts/populate_analytics_test_data.sh`
6. Archive change; check off TASKS.md 6.4

**Rollback:** Revert frontend commits; backend unchanged.

## Open Questions

None — full backend scope and UTC date display confirmed during planning.
