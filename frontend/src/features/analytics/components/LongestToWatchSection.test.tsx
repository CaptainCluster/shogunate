import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import * as analyticsApi from '../../../api/analyticsApi'
import { LongestToWatchSection } from './LongestToWatchSection'

vi.mock('../../../api/analyticsApi', () => ({
  getLongestToWatch: vi.fn(),
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
        <LongestToWatchSection />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('LongestToWatchSection', () => {
  it('renders ranked list with durations', async () => {
    vi.mocked(analyticsApi.getLongestToWatch).mockResolvedValue([
      {
        showId: 'show-1',
        title: 'Slow Burn Show',
        durationSeconds: 86_400,
        firstWatchedAt: '2024-01-01T00:00:00Z',
        lastWatchedAt: '2024-01-02T00:00:00Z',
      },
    ])

    renderSection()

    expect(await screen.findByText('Slow Burn Show')).toBeInTheDocument()
    expect(screen.getByText('1d')).toBeInTheDocument()
  })

  it('shows empty state when no shows', async () => {
    vi.mocked(analyticsApi.getLongestToWatch).mockResolvedValue([])

    renderSection()

    expect(await screen.findByText('No shows with watch history yet.')).toBeInTheDocument()
  })
})
