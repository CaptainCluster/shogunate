import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import * as showApi from '../../api/showApi'
import { SearchPage } from './SearchPage'

vi.mock('../../api/showApi', () => ({
  searchShows: vi.fn(),
  addShow: vi.fn(),
}))

function renderSearchPage() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <SearchPage />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('SearchPage', () => {
  it('submits a search and renders results', async () => {
    const user = userEvent.setup()
    vi.mocked(showApi.searchShows).mockResolvedValue([
      {
        tvmazeId: 1,
        title: 'Test Show',
        overview: 'A test show',
        posterUrl: null,
        tvmazeUrl: null,
        firstAirDate: null,
      },
    ])

    renderSearchPage()

    await user.type(screen.getByRole('searchbox', { name: 'Search shows' }), 'test')
    await user.click(screen.getByRole('button', { name: 'Search' }))

    await waitFor(() => expect(screen.getByText('Test Show')).toBeInTheDocument())
    expect(showApi.searchShows).toHaveBeenCalledWith('test')
  })
})
