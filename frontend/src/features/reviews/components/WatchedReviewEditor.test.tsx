import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, waitFor, within } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../../api/client'
import * as reviewApi from '../../../api/reviewApi'
import { WatchedReviewEditor } from './WatchedReviewEditor'

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

describe('WatchedReviewEditor', () => {
  it('renders nothing when the target is not watched', () => {
    const { container } = renderWithProviders(
      <WatchedReviewEditor watched={false} targetType="EPISODE" targetId="episode-1" />,
    )

    expect(container).toBeEmptyDOMElement()
    expect(reviewApi.getReview).not.toHaveBeenCalled()
  })

  it('renders the review editor when the target is watched', async () => {
    vi.mocked(reviewApi.getReview).mockRejectedValue(new ApiError('Not found', 404))

    const { container } = renderWithProviders(
      <WatchedReviewEditor watched targetType="EPISODE" targetId="episode-1" />,
    )

    await waitFor(() =>
      expect(within(container).getByRole('slider')).toBeInTheDocument(),
    )
  })
})
