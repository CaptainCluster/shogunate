import { apiRequest } from './client'

export function markEpisodeWatched(id: string) {
  return apiRequest<void>(`/api/watch/episodes/${id}`, { method: 'POST' })
}

export function unmarkEpisodeWatched(id: string) {
  return apiRequest<void>(`/api/watch/episodes/${id}`, { method: 'DELETE' })
}

export function markSeasonWatched(id: string) {
  return apiRequest<void>(`/api/watch/seasons/${id}`, { method: 'POST' })
}

export function unmarkSeasonWatched(id: string, confirm: boolean) {
  const params = new URLSearchParams({ confirm: String(confirm) })
  return apiRequest<void>(`/api/watch/seasons/${id}?${params}`, { method: 'DELETE' })
}

export function markShowWatched(id: string) {
  return apiRequest<void>(`/api/watch/shows/${id}`, { method: 'POST' })
}

export function unmarkShowWatched(id: string, confirm: boolean) {
  const params = new URLSearchParams({ confirm: String(confirm) })
  return apiRequest<void>(`/api/watch/shows/${id}?${params}`, { method: 'DELETE' })
}
