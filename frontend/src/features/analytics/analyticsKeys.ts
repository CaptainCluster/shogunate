import type { AnalyticsPeriod } from '../../api/analyticsApi'

export const analyticsKeys = {
  all: ['analytics'] as const,
  totals: () => [...analyticsKeys.all, 'totals'] as const,
  watchCounts: (period: AnalyticsPeriod, from: string, to?: string) =>
    [...analyticsKeys.all, 'watch-counts', period, from, to ?? null] as const,
  longestToWatch: () => [...analyticsKeys.all, 'longest-to-watch'] as const,
  favorites: () => [...analyticsKeys.all, 'favorites'] as const,
  watchStreaks: () => [...analyticsKeys.all, 'watch-streaks'] as const,
  libraryCompletion: () => [...analyticsKeys.all, 'library-completion'] as const,
  planToWatchCount: () => [...analyticsKeys.all, 'plan-to-watch-count'] as const,
}
