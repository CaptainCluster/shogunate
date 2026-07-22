import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { renderHook, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import * as favoriteApi from '../../../api/favoriteApi'
import { favoriteKeys } from '../favoriteKeys'
import { useFavoriteMutations } from './useFavoriteMutations'

vi.mock('../../../api/favoriteApi', () => ({
  addFavorite: vi.fn(),
  removeFavorite: vi.fn(),
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

describe('useFavoriteMutations', () => {
  it('adds a favorite and invalidates related queries', async () => {
    vi.mocked(favoriteApi.addFavorite).mockResolvedValue({
      id: 'fav-1',
      showId: 'show-1',
      createdAt: '2024-01-01T00:00:00Z',
    })

    const { wrapper, queryClient } = createWrapper()
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries')

    const { result } = renderHook(() => useFavoriteMutations('show-1'), { wrapper })

    result.current.addFavorite.mutate()

    await waitFor(() => expect(result.current.addFavorite.isSuccess).toBe(true))

    expect(favoriteApi.addFavorite).toHaveBeenCalledWith('show-1')
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: favoriteKeys.list() })
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: favoriteKeys.suggestions() })
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: favoriteKeys.status('show-1') })
  })

  it('removes a favorite via API', async () => {
    vi.mocked(favoriteApi.removeFavorite).mockResolvedValue(undefined)

    const { wrapper } = createWrapper()
    const { result } = renderHook(() => useFavoriteMutations('show-1'), { wrapper })

    result.current.removeFavorite.mutate()

    await waitFor(() => expect(result.current.removeFavorite.isSuccess).toBe(true))
    expect(favoriteApi.removeFavorite).toHaveBeenCalledWith('show-1')
  })
})
