import { apiRequest } from './client'

export type LibraryStatus = 'NONE' | 'PLAN_TO_WATCH'

export interface ShowSearchResult {
  tvmazeId: number
  title: string
  overview: string | null
  posterUrl: string | null
  tvmazeUrl: string | null
  firstAirDate: string | null
}

export interface ShowSummary {
  id: string
  tvmazeId: number
  title: string
  overview: string | null
  posterUrl: string | null
  tvmazeUrl: string | null
  firstAirDate: string | null
  libraryStatus: LibraryStatus
  addedAt: string
}

export interface Episode {
  id: string
  episodeNumber: number
  title: string | null
  airDate: string | null
  watched: boolean
  watchedAt: string | null
}

export interface Season {
  id: string
  seasonNumber: number
  name: string | null
  watched: boolean
  watchedAt: string | null
  episodes: Episode[]
}

export interface ShowDetail extends ShowSummary {
  watched: boolean
  watchedAt: string | null
  seasons: Season[]
}

export function searchShows(query: string) {
  const params = new URLSearchParams({ query })
  return apiRequest<ShowSearchResult[]>(`/api/shows/search?${params}`)
}

export function listLibrary() {
  return apiRequest<ShowSummary[]>('/api/shows')
}

export function getShow(id: string) {
  return apiRequest<ShowDetail>(`/api/shows/${id}`)
}

export function addShow(tvmazeId: number) {
  return apiRequest<ShowDetail>('/api/shows', {
    method: 'POST',
    body: JSON.stringify({ tvmazeId }),
  })
}

export function updateLibraryStatus(id: string, libraryStatus: LibraryStatus) {
  return apiRequest<ShowSummary>(`/api/shows/${id}`, {
    method: 'PATCH',
    body: JSON.stringify({ libraryStatus }),
  })
}

export function removeShow(id: string) {
  return apiRequest<void>(`/api/shows/${id}`, {
    method: 'DELETE',
  })
}
