import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { renderHook, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import * as favoriteApi from '../../../api/favoriteApi'
import { useFavoriteStatus } from './useFavorites'

vi.mock('../../../api/favoriteApi', () => ({
  getFavoriteStatus: vi.fn(),
}))

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  })

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  )
}

describe('useFavoriteStatus', () => {
  it('returns favorite status from API', async () => {
    vi.mocked(favoriteApi.getFavoriteStatus).mockResolvedValue({
      isFavorite: true,
      isSuggested: false,
    })

    const { result } = renderHook(() => useFavoriteStatus('show-1'), {
      wrapper: createWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual({ isFavorite: true, isSuggested: false })
    expect(favoriteApi.getFavoriteStatus).toHaveBeenCalledWith('show-1')
  })
})
