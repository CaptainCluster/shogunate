import { useMutation, useQueryClient } from '@tanstack/react-query'
import * as favoriteApi from '../../../api/favoriteApi'
import { favoriteKeys } from '../favoriteKeys'

function invalidateFavoriteQueries(
  queryClient: ReturnType<typeof useQueryClient>,
  showId: string,
) {
  return Promise.all([
    queryClient.invalidateQueries({ queryKey: favoriteKeys.list() }),
    queryClient.invalidateQueries({ queryKey: favoriteKeys.suggestions() }),
    queryClient.invalidateQueries({ queryKey: favoriteKeys.status(showId) }),
  ])
}

export function useFavoriteMutations(showId: string) {
  const queryClient = useQueryClient()

  const addFavorite = useMutation({
    mutationFn: () => favoriteApi.addFavorite(showId),
    onSuccess: () => invalidateFavoriteQueries(queryClient, showId),
  })

  const removeFavorite = useMutation({
    mutationFn: () => favoriteApi.removeFavorite(showId),
    onSuccess: () => invalidateFavoriteQueries(queryClient, showId),
  })

  const error = addFavorite.error ?? removeFavorite.error
  const isPending = addFavorite.isPending || removeFavorite.isPending

  return {
    addFavorite,
    removeFavorite,
    error,
    isPending,
  }
}

export function invalidateAllFavoriteQueries(queryClient: ReturnType<typeof useQueryClient>) {
  return queryClient.invalidateQueries({ queryKey: favoriteKeys.all })
}
