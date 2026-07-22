import { useQuery } from '@tanstack/react-query'
import * as favoriteApi from '../../../api/favoriteApi'
import { favoriteKeys } from '../favoriteKeys'

export function useFavorites() {
  return useQuery({
    queryKey: favoriteKeys.list(),
    queryFn: favoriteApi.listFavorites,
  })
}

export function useFavoriteSuggestions() {
  return useQuery({
    queryKey: favoriteKeys.suggestions(),
    queryFn: favoriteApi.getSuggestions,
  })
}

export function useFavoriteStatus(showId: string) {
  return useQuery({
    queryKey: favoriteKeys.status(showId),
    queryFn: () => favoriteApi.getFavoriteStatus(showId),
    enabled: !!showId,
  })
}
