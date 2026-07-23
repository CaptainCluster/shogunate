import { useQuery, useQueryClient } from '@tanstack/react-query'
import type { AnalyticsPeriod } from '../../../api/analyticsApi'
import * as analyticsApi from '../../../api/analyticsApi'
import { analyticsKeys } from '../analyticsKeys'

export function useAnalyticsTotals() {
  return useQuery({
    queryKey: analyticsKeys.totals(),
    queryFn: analyticsApi.getTotals,
  })
}

export function useWatchCounts(period: AnalyticsPeriod, from: string, to?: string) {
  return useQuery({
    queryKey: analyticsKeys.watchCounts(period, from, to),
    queryFn: () => analyticsApi.getWatchCounts(period, from, to),
    enabled: period !== 'CUSTOM' || !!to,
  })
}

export function useLongestToWatch() {
  return useQuery({
    queryKey: analyticsKeys.longestToWatch(),
    queryFn: analyticsApi.getLongestToWatch,
  })
}

export function useAnalyticsFavorites() {
  return useQuery({
    queryKey: analyticsKeys.favorites(),
    queryFn: analyticsApi.getAnalyticsFavorites,
  })
}

export function useWatchStreaks() {
  return useQuery({
    queryKey: analyticsKeys.watchStreaks(),
    queryFn: analyticsApi.getWatchStreaks,
  })
}

export function useLibraryCompletion() {
  return useQuery({
    queryKey: analyticsKeys.libraryCompletion(),
    queryFn: analyticsApi.getLibraryCompletion,
  })
}

export function usePlanToWatchCount() {
  return useQuery({
    queryKey: analyticsKeys.planToWatchCount(),
    queryFn: analyticsApi.getPlanToWatchCount,
  })
}

export function invalidateAllAnalyticsQueries(queryClient: ReturnType<typeof useQueryClient>) {
  return queryClient.invalidateQueries({ queryKey: analyticsKeys.all })
}
