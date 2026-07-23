import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import * as favoriteApi from '../../../api/favoriteApi'
import * as showApi from '../../../api/showApi'
import { renderWithI18n } from '../../../test/renderWithI18n'
import { FavoriteSuggestionsPanel } from './FavoriteSuggestionsPanel'

vi.mock('../../../api/favoriteApi', () => ({
  getSuggestions: vi.fn(),
  addFavorite: vi.fn(),
}))

vi.mock('../../../api/showApi', () => ({
  listLibrary: vi.fn(),
}))

function renderPanel() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })

  return renderWithI18n(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <FavoriteSuggestionsPanel />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('FavoriteSuggestionsPanel', () => {
  it('renders suggested shows with opt-in add buttons', async () => {
    vi.mocked(favoriteApi.getSuggestions).mockResolvedValue([
      { showId: 'show-1', weightedScore: 5 },
    ])
    vi.mocked(showApi.listLibrary).mockResolvedValue([
      {
        id: 'show-1',
        tvmazeId: 1,
        title: 'Suggested Show',
        overview: null,
        posterUrl: null,
        tvmazeUrl: null,
        firstAirDate: null,
        libraryStatus: 'NONE',
        addedAt: '2024-01-01T00:00:00Z',
      },
    ])

    renderPanel()

    expect(await screen.findByText('Suggested Show')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Add to favorites' })).toBeInTheDocument()
  })

  it('calls add favorite when opt-in button is clicked', async () => {
    const user = userEvent.setup()
    vi.mocked(favoriteApi.getSuggestions).mockResolvedValue([
      { showId: 'show-1', weightedScore: 5 },
    ])
    vi.mocked(showApi.listLibrary).mockResolvedValue([
      {
        id: 'show-1',
        tvmazeId: 1,
        title: 'Suggested Show',
        overview: null,
        posterUrl: null,
        tvmazeUrl: null,
        firstAirDate: null,
        libraryStatus: 'NONE',
        addedAt: '2024-01-01T00:00:00Z',
      },
    ])
    vi.mocked(favoriteApi.addFavorite).mockResolvedValue({
      id: 'fav-1',
      showId: 'show-1',
      createdAt: '2024-01-01T00:00:00Z',
    })

    renderPanel()

    await user.click(await screen.findByRole('button', { name: 'Add to favorites' }))

    await waitFor(() => expect(favoriteApi.addFavorite).toHaveBeenCalledWith('show-1'))
  })

  it('renders nothing when there are no suggestions', async () => {
    vi.mocked(favoriteApi.getSuggestions).mockResolvedValue([])
    vi.mocked(showApi.listLibrary).mockResolvedValue([])

    const { container } = renderPanel()

    await waitFor(() => expect(favoriteApi.getSuggestions).toHaveBeenCalled())
    expect(container).toBeEmptyDOMElement()
  })
})
