## Why

Phase 6 backend (`analytics-backend`) shipped seven read-only analytics REST endpoints, but users still cannot view statistics derived from their watch history — the core Phase 6.4 capability from the PRD. This change delivers the analytics dashboard frontend so authenticated users can explore their personal watch metrics in one place.

## What Changes

- Add `analyticsApi.ts` and `features/analytics/` (TanStack Query hooks, formatters, dashboard components)
- Protected `/analytics` route with a single-page dashboard consuming all seven `/api/analytics` endpoints
- Dashboard sections: totals, watch counts by period (with period selector), longest-to-watch ranking, favorites list, watch streaks, library completion, plan-to-watch count
- CSS bar charts for watch counts and completion (no new chart npm dependency)
- UTC date/time display with explicit labels (matches backend streak and period logic)
- Nav link in layout, home page link, and analytics query invalidation from watch/favorite/library mutations
- Vitest tests for formatters, key dashboard sections, and invalidation

## Capabilities

### New Capabilities

_None — frontend implements existing analytics requirements._

### Modified Capabilities

- `analytics`: Add UI requirements for the analytics dashboard route, period selector, ranked lists, favorites display, loading/error/empty states, and UTC date formatting

## Impact

- **Frontend:** new `api/analyticsApi.ts`, `features/analytics/`; updates to `router.tsx`, `Layout.tsx`, `HomePage.tsx`, watch/favorite/library mutation hooks
- **Backend:** none (consumes existing endpoints)
- **Dependencies:** none (no new npm packages)
- **Docs:** check off Phase 6.4 in `openspec/TASKS.md` on archive

## Non-goals

- Streak history calendar / heatmap UI
- Plan-to-watch show list (count only; backend returns count, not a filtered list)
- Enriching favorites with show titles on the backend (client joins with library list)
- Dedicated `/favorites` route or embedding analytics on the home page
- Local-time date conversion (UTC only)
