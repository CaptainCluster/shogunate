import { useMutation, useQueryClient } from '@tanstack/react-query'
import * as watchApi from '../../../api/watchApi'
import { showKeys } from '../../library/showKeys'
import type { WatchTargetType } from '../watchKeys'

export interface PendingWatchAction {
  targetType: WatchTargetType
  targetId: string
  action: 'mark' | 'unmark'
}

export function useWatchMutations(showId: string) {
  const queryClient = useQueryClient()

  function invalidateShowDetail() {
    return queryClient.invalidateQueries({ queryKey: showKeys.detail(showId) })
  }

  const markEpisode = useMutation({
    mutationFn: (episodeId: string) => watchApi.markEpisodeWatched(episodeId),
    onSuccess: invalidateShowDetail,
  })

  const unmarkEpisode = useMutation({
    mutationFn: (episodeId: string) => watchApi.unmarkEpisodeWatched(episodeId),
    onSuccess: invalidateShowDetail,
  })

  const markSeason = useMutation({
    mutationFn: (seasonId: string) => watchApi.markSeasonWatched(seasonId),
    onSuccess: invalidateShowDetail,
  })

  const unmarkSeason = useMutation({
    mutationFn: (seasonId: string) => watchApi.unmarkSeasonWatched(seasonId, true),
    onSuccess: invalidateShowDetail,
  })

  const markShow = useMutation({
    mutationFn: (id: string) => watchApi.markShowWatched(id),
    onSuccess: invalidateShowDetail,
  })

  const unmarkShow = useMutation({
    mutationFn: (id: string) => watchApi.unmarkShowWatched(id, true),
    onSuccess: invalidateShowDetail,
  })

  const mutations = [
    markEpisode,
    unmarkEpisode,
    markSeason,
    unmarkSeason,
    markShow,
    unmarkShow,
  ]

  const isPending = mutations.some((mutation) => mutation.isPending)

  function getPendingAction(): PendingWatchAction | null {
    if (markEpisode.isPending && markEpisode.variables !== undefined) {
      return { targetType: 'EPISODE', targetId: markEpisode.variables, action: 'mark' }
    }
    if (unmarkEpisode.isPending && unmarkEpisode.variables !== undefined) {
      return { targetType: 'EPISODE', targetId: unmarkEpisode.variables, action: 'unmark' }
    }
    if (markSeason.isPending && markSeason.variables !== undefined) {
      return { targetType: 'SEASON', targetId: markSeason.variables, action: 'mark' }
    }
    if (unmarkSeason.isPending && unmarkSeason.variables !== undefined) {
      return { targetType: 'SEASON', targetId: unmarkSeason.variables, action: 'unmark' }
    }
    if (markShow.isPending && markShow.variables !== undefined) {
      return { targetType: 'SHOW', targetId: markShow.variables, action: 'mark' }
    }
    if (unmarkShow.isPending && unmarkShow.variables !== undefined) {
      return { targetType: 'SHOW', targetId: unmarkShow.variables, action: 'unmark' }
    }
    return null
  }

  const error =
    markEpisode.error ??
    unmarkEpisode.error ??
    markSeason.error ??
    unmarkSeason.error ??
    markShow.error ??
    unmarkShow.error

  return {
    markEpisode,
    unmarkEpisode,
    markSeason,
    unmarkSeason,
    markShow,
    unmarkShow,
    isPending,
    pendingAction: getPendingAction(),
    error,
  }
}
