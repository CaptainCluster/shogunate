import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../../api/client'
import * as reviewApi from '../../../api/reviewApi'
import { ReviewEditor } from './ReviewEditor'

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

describe('ReviewEditor', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows only stars until a rating is selected', async () => {
    vi.mocked(reviewApi.getReview).mockRejectedValue(new ApiError('Not found', 404))

    const { container } = renderWithProviders(
      <ReviewEditor targetType="SHOW" targetId="show-1" />,
    )
    const view = within(container)

    await waitFor(() => expect(view.getByRole('slider')).toBeInTheDocument())
    expect(view.queryByRole('textbox')).not.toBeInTheDocument()
    expect(view.queryByRole('button', { name: 'Save review' })).not.toBeInTheDocument()
  })

  it('creates a review when none exists', async () => {
    const user = userEvent.setup()
    vi.mocked(reviewApi.getReview).mockRejectedValue(new ApiError('Not found', 404))
    vi.mocked(reviewApi.createReview).mockResolvedValue({
      id: 'review-1',
      targetType: 'SHOW',
      targetId: 'show-1',
      rating: 4,
      body: 'Nice',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: null,
    })

    const { container } = renderWithProviders(
      <ReviewEditor targetType="SHOW" targetId="show-1" />,
    )
    const view = within(container)

    await waitFor(() => expect(view.getByRole('slider')).toBeInTheDocument())

    await user.click(container.querySelectorAll('.star-rating__half--left')[3]!)
    await waitFor(() => expect(view.getByRole('textbox')).toBeInTheDocument())
    await user.type(view.getByRole('textbox'), 'Nice')
    await user.click(view.getByRole('button', { name: 'Save review' }))

    await waitFor(() =>
      expect(reviewApi.createReview).toHaveBeenCalledWith({
        targetType: 'SHOW',
        targetId: 'show-1',
        rating: 4,
        body: 'Nice',
      }),
    )
  })

  it('updates and deletes an existing review', async () => {
    const user = userEvent.setup()
    vi.mocked(reviewApi.getReview).mockResolvedValue({
      id: 'review-1',
      targetType: 'SHOW',
      targetId: 'show-1',
      rating: 3,
      body: 'Old',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: null,
    })
    vi.mocked(reviewApi.updateReview).mockResolvedValue({
      id: 'review-1',
      targetType: 'SHOW',
      targetId: 'show-1',
      rating: 3.5,
      body: 'Updated',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-02T00:00:00Z',
    })
    vi.mocked(reviewApi.deleteReview).mockResolvedValue(undefined)

    const { container } = renderWithProviders(
      <ReviewEditor targetType="SHOW" targetId="show-1" />,
    )
    const view = within(container)

    await waitFor(() => expect(view.getByDisplayValue('Old')).toBeInTheDocument())

    fireEvent.change(view.getByRole('textbox'), { target: { value: 'Updated' } })
    await user.click(view.getByRole('button', { name: 'Save review' }))

    await waitFor(() =>
      expect(reviewApi.updateReview).toHaveBeenCalledWith('review-1', {
        rating: 3,
        body: 'Updated',
      }),
    )

    await user.click(view.getByRole('button', { name: 'Delete review' }))
    await waitFor(() => expect(reviewApi.deleteReview).toHaveBeenCalledWith('review-1'))
  })

  it('collapses existing episode reviews until the expand control is used', async () => {
    const user = userEvent.setup()
    vi.mocked(reviewApi.getReview).mockResolvedValue({
      id: 'review-1',
      targetType: 'EPISODE',
      targetId: 'episode-1',
      rating: 4,
      body: 'Solid episode',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: null,
    })

    const { container } = renderWithProviders(
      <ReviewEditor
        compact
        collapseExistingReview
        targetType="EPISODE"
        targetId="episode-1"
      />,
    )
    const view = within(container)

    await waitFor(() => expect(view.getByRole('slider')).toBeInTheDocument())
    expect(view.queryByRole('textbox')).not.toBeInTheDocument()
    expect(view.getByRole('button', { name: 'Show review details' })).toBeInTheDocument()

    await user.click(view.getByRole('button', { name: 'Show review details' }))

    await waitFor(() => expect(view.getByDisplayValue('Solid episode')).toBeInTheDocument())
    expect(view.getByRole('button', { name: 'Save review' })).toBeInTheDocument()
  })
})
