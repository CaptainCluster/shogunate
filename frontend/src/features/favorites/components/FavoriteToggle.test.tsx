import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import * as favoriteApi from '../../../api/favoriteApi'
import { renderWithI18n } from '../../../test/renderWithI18n'
import { FavoriteToggle } from './FavoriteToggle'

vi.mock('../../../api/favoriteApi', () => ({
  getFavoriteStatus: vi.fn(),
  addFavorite: vi.fn(),
  removeFavorite: vi.fn(),
}))

function renderToggle() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })

  return renderWithI18n(
    <QueryClientProvider client={queryClient}>
      <FavoriteToggle showId="show-1" />
    </QueryClientProvider>,
  )
}

describe('FavoriteToggle', () => {
  it('shows suggestion badge and add button when suggested but not favorited', async () => {
    vi.mocked(favoriteApi.getFavoriteStatus).mockResolvedValue({
      isFavorite: false,
      isSuggested: true,
    })

    renderToggle()

    expect(await screen.findByText('Suggested favorite')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Add to favorites' })).toBeInTheDocument()
  })

  it('adds favorite when toggle is clicked', async () => {
    const user = userEvent.setup()
    vi.mocked(favoriteApi.getFavoriteStatus).mockResolvedValue({
      isFavorite: false,
      isSuggested: false,
    })
    vi.mocked(favoriteApi.addFavorite).mockResolvedValue({
      id: 'fav-1',
      showId: 'show-1',
      createdAt: '2024-01-01T00:00:00Z',
    })

    renderToggle()

    await user.click(await screen.findByRole('button', { name: 'Add to favorites' }))

    await waitFor(() => expect(favoriteApi.addFavorite).toHaveBeenCalledWith('show-1'))
  })
})
