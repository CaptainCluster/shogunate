import { apiRequest } from './client'

export interface Favorite {
  id: string
  showId: string
  createdAt: string
}

export interface FavoriteSuggestion {
  showId: string
  weightedScore: number
}

export interface FavoriteStatus {
  isFavorite: boolean
  isSuggested: boolean
}

export function listFavorites() {
  return apiRequest<Favorite[]>('/api/favorites')
}

export function getSuggestions() {
  return apiRequest<FavoriteSuggestion[]>('/api/favorites/suggestions')
}

export function getFavoriteStatus(showId: string) {
  const params = new URLSearchParams({ showId })
  return apiRequest<FavoriteStatus>(`/api/favorites/status?${params}`)
}

export function addFavorite(showId: string) {
  return apiRequest<Favorite>('/api/favorites', {
    method: 'POST',
    body: JSON.stringify({ showId }),
  })
}

export function removeFavorite(showId: string) {
  return apiRequest<void>(`/api/favorites/${showId}`, {
    method: 'DELETE',
  })
}
