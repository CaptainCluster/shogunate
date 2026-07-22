import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { renderHook, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../../api/client'
import * as reviewApi from '../../../api/reviewApi'
import { useReview } from './useReview'

vi.mock('../../../api/reviewApi', () => ({
  getReview: vi.fn(),
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

describe('useReview', () => {
  it('returns null when review is not found', async () => {
    vi.mocked(reviewApi.getReview).mockRejectedValue(new ApiError('Not found', 404))

    const { result } = renderHook(() => useReview('SHOW', 'show-1'), {
      wrapper: createWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toBeNull()
  })

  it('returns review data when found', async () => {
    vi.mocked(reviewApi.getReview).mockResolvedValue({
      id: 'review-1',
      targetType: 'SHOW',
      targetId: 'show-1',
      rating: 4,
      body: 'Great show',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: null,
    })

    const { result } = renderHook(() => useReview('SHOW', 'show-1'), {
      wrapper: createWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.rating).toBe(4)
  })
})
