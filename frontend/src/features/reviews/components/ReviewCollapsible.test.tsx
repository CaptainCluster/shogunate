import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import * as reviewApi from '../../../api/reviewApi'
import { ReviewCollapsible } from './ReviewCollapsible'

vi.mock('../../../api/reviewApi', () => ({
  getReview: vi.fn(),
  createReview: vi.fn(),
  updateReview: vi.fn(),
  deleteReview: vi.fn(),
}))

function renderWithProviders(ui: React.ReactElement) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })

  return render(<QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>)
}

describe('ReviewCollapsible', () => {
  it('is collapsed by default and shows stars without numeric text when a review exists', async () => {
    vi.mocked(reviewApi.getReview).mockResolvedValue({
      id: 'review-1',
      targetType: 'EPISODE',
      targetId: 'episode-1',
      rating: 4.5,
      body: 'Solid episode',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: null,
    })

    const { container } = renderWithProviders(
      <ReviewCollapsible targetType="EPISODE" targetId="episode-1" />,
    )

    await waitFor(() => expect(screen.getByText('Review')).toBeInTheDocument())

    await waitFor(() => {
      const summary = container.querySelector('.review-collapsible__summary')
      expect(summary?.querySelectorAll('.star-rating__star--full')).toHaveLength(4)
      expect(summary?.querySelectorAll('.star-rating__star--half')).toHaveLength(1)
      expect(summary?.textContent).not.toMatch(/4\.5/)
    })

    expect(container.querySelector('details')?.open).toBe(false)
  })
})
