import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import * as analyticsApi from '../../../api/analyticsApi'
import * as showApi from '../../../api/showApi'
import { FavoritesSection } from './FavoritesSection'

vi.mock('../../../api/analyticsApi', () => ({
  getAnalyticsFavorites: vi.fn(),
}))

vi.mock('../../../api/showApi', () => ({
  listLibrary: vi.fn(),
}))

function renderSection() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <FavoritesSection />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('FavoritesSection', () => {
  it('joins favorites with library titles', async () => {
    vi.mocked(analyticsApi.getAnalyticsFavorites).mockResolvedValue([
      { id: 'fav-1', showId: 'show-1', createdAt: '2024-01-01T00:00:00Z' },
    ])
    vi.mocked(showApi.listLibrary).mockResolvedValue([
      {
        id: 'show-1',
        tvmazeId: 1,
        title: 'Favorite Show',
        overview: null,
        posterUrl: null,
        tvmazeUrl: null,
        firstAirDate: null,
        libraryStatus: 'NONE',
        addedAt: '2024-01-01T00:00:00Z',
      },
    ])

    renderSection()

    expect(await screen.findByText('Favorite Show')).toBeInTheDocument()
  })

  it('shows empty state when no favorites', async () => {
    vi.mocked(analyticsApi.getAnalyticsFavorites).mockResolvedValue([])
    vi.mocked(showApi.listLibrary).mockResolvedValue([])

    renderSection()

    expect(await screen.findByText(/No favorites yet/)).toBeInTheDocument()
  })
})
