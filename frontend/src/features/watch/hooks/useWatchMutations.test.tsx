import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { renderHook, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import * as watchApi from '../../../api/watchApi'
import { showKeys } from '../../library/showKeys'
import { useWatchMutations } from './useWatchMutations'

vi.mock('../../../api/watchApi', () => ({
  markEpisodeWatched: vi.fn(),
  unmarkEpisodeWatched: vi.fn(),
  markSeasonWatched: vi.fn(),
  unmarkSeasonWatched: vi.fn(),
  markShowWatched: vi.fn(),
  unmarkShowWatched: vi.fn(),
}))

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })

  return {
    queryClient,
    wrapper: ({ children }: { children: React.ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    ),
  }
}

describe('useWatchMutations', () => {
  it('calls markEpisodeWatched and invalidates show detail', async () => {
    vi.mocked(watchApi.markEpisodeWatched).mockResolvedValue(undefined)
    const { wrapper, queryClient } = createWrapper()
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries')

    const { result } = renderHook(() => useWatchMutations('show-1'), { wrapper })

    result.current.markEpisode.mutate('episode-1')

    await waitFor(() => expect(result.current.markEpisode.isSuccess).toBe(true))

    expect(watchApi.markEpisodeWatched).toHaveBeenCalledWith('episode-1')
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: showKeys.detail('show-1') })
  })

  it('calls unmarkSeasonWatched with confirm=true', async () => {
    vi.mocked(watchApi.unmarkSeasonWatched).mockResolvedValue(undefined)
    const { wrapper } = createWrapper()

    const { result } = renderHook(() => useWatchMutations('show-1'), { wrapper })

    result.current.unmarkSeason.mutate('season-1')

    await waitFor(() => expect(result.current.unmarkSeason.isSuccess).toBe(true))

    expect(watchApi.unmarkSeasonWatched).toHaveBeenCalledWith('season-1', true)
  })
})
