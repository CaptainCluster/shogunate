import { apiRequest } from './client'

export type AnalyticsPeriod = 'MONTH' | 'YEAR' | 'CUSTOM'

export interface TargetTypeCounts {
  episodes: number
  seasons: number
  shows: number
}

export interface TotalsResponse {
  counts: TargetTypeCounts
}

export interface WatchCountsResponse {
  period: AnalyticsPeriod
  from: string
  to: string
  counts: TargetTypeCounts
}

export interface LongestToWatchEntry {
  showId: string
  title: string
  durationSeconds: number
  firstWatchedAt: string
  lastWatchedAt: string
}

export interface AnalyticsFavorite {
  id: string
  showId: string
  createdAt: string
}

export interface WatchStreaksResponse {
  currentStreakDays: number
  longestStreakDays: number
  currentStreakStartDate: string | null
  longestStreakStartDate: string | null
  longestStreakEndDate: string | null
}

export interface ShowCompletionEntry {
  showId: string
  title: string
  watchedEpisodes: number
  totalEpisodes: number
  completionPercent: number
  fullyWatched: boolean
}

export interface LibraryCompletionResponse {
  overallCompletionPercent: number
  watchedEpisodes: number
  totalEpisodes: number
  shows: ShowCompletionEntry[]
}

export interface PlanToWatchCountResponse {
  count: number
}

export function getTotals() {
  return apiRequest<TotalsResponse>('/api/analytics/totals')
}

export function getWatchCounts(period: AnalyticsPeriod, from: string, to?: string) {
  const params = new URLSearchParams({ period, from })
  if (to) {
    params.set('to', to)
  }
  return apiRequest<WatchCountsResponse>(`/api/analytics/watch-counts?${params}`)
}

export function getLongestToWatch() {
  return apiRequest<LongestToWatchEntry[]>('/api/analytics/longest-to-watch')
}

export function getAnalyticsFavorites() {
  return apiRequest<AnalyticsFavorite[]>('/api/analytics/favorites')
}

export function getWatchStreaks() {
  return apiRequest<WatchStreaksResponse>('/api/analytics/watch-streaks')
}

export function getLibraryCompletion() {
  return apiRequest<LibraryCompletionResponse>('/api/analytics/library-completion')
}

export function getPlanToWatchCount() {
  return apiRequest<PlanToWatchCountResponse>('/api/analytics/plan-to-watch-count')
}
