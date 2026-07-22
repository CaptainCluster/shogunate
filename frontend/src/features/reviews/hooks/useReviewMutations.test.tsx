import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { renderHook, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import * as reviewApi from '../../../api/reviewApi'
import { reviewKeys } from '../reviewKeys'
import { useReviewMutations } from './useReviewMutations'

vi.mock('../../../api/reviewApi', () => ({
  createReview: vi.fn(),
  updateReview: vi.fn(),
  deleteReview: vi.fn(),
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

describe('useReviewMutations', () => {
  it('creates a review and invalidates the target query', async () => {
    vi.mocked(reviewApi.createReview).mockResolvedValue({
      id: 'review-1',
      targetType: 'SHOW',
      targetId: 'show-1',
      rating: 3.5,
      body: null,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: null,
    })

    const { wrapper, queryClient } = createWrapper()
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries')

    const { result } = renderHook(() => useReviewMutations('SHOW', 'show-1'), { wrapper })

    result.current.createReview.mutate({ rating: 3.5, body: null })

    await waitFor(() => expect(result.current.createReview.isSuccess).toBe(true))

    expect(reviewApi.createReview).toHaveBeenCalledWith({
      targetType: 'SHOW',
      targetId: 'show-1',
      rating: 3.5,
      body: null,
    })
    expect(invalidateSpy).toHaveBeenCalledWith({
      queryKey: reviewKeys.target('SHOW', 'show-1'),
    })
  })

  it('updates and deletes reviews via API', async () => {
    vi.mocked(reviewApi.updateReview).mockResolvedValue({
      id: 'review-1',
      targetType: 'SEASON',
      targetId: 'season-1',
      rating: 5,
      body: 'Updated',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-02T00:00:00Z',
    })
    vi.mocked(reviewApi.deleteReview).mockResolvedValue(undefined)

    const { wrapper } = createWrapper()
    const { result } = renderHook(() => useReviewMutations('SEASON', 'season-1'), { wrapper })

    result.current.updateReview.mutate({ id: 'review-1', rating: 5, body: 'Updated' })
    await waitFor(() => expect(result.current.updateReview.isSuccess).toBe(true))
    expect(reviewApi.updateReview).toHaveBeenCalledWith('review-1', {
      rating: 5,
      body: 'Updated',
    })

    result.current.deleteReview.mutate('review-1')
    await waitFor(() => expect(result.current.deleteReview.isSuccess).toBe(true))
    expect(reviewApi.deleteReview).toHaveBeenCalledWith('review-1')
  })
})
