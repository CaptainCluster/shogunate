import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import * as analyticsApi from '../../../api/analyticsApi'
import { renderWithI18n } from '../../../test/renderWithI18n'
import { WatchCountsSection } from './WatchCountsSection'

vi.mock('../../../api/analyticsApi', () => ({
  getWatchCounts: vi.fn(),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

function renderSection() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })

  return renderWithI18n(
    <QueryClientProvider client={queryClient}>
      <WatchCountsSection />
    </QueryClientProvider>,
  )
}

describe('WatchCountsSection', () => {
  it('renders watch count bars for the default month period', async () => {
    vi.mocked(analyticsApi.getWatchCounts).mockResolvedValue({
      period: 'MONTH',
      from: '2024-06-01',
      to: '2024-06-30',
      counts: { episodes: 12, seasons: 3, shows: 1 },
    })

    renderSection()

    expect(await screen.findByText('12')).toBeInTheDocument()
    expect(screen.getByText('3')).toBeInTheDocument()
    expect(screen.getByText('1')).toBeInTheDocument()
    expect(screen.getByLabelText('Watch counts by target type')).toBeInTheDocument()
  })

  it('switches to custom period and requests with to date', async () => {
    vi.mocked(analyticsApi.getWatchCounts).mockResolvedValue({
      period: 'CUSTOM',
      from: '2024-01-01',
      to: '2024-01-31',
      counts: { episodes: 5, seasons: 2, shows: 1 },
    })

    renderSection()

    await userEvent.click(await screen.findByRole('button', { name: 'Custom' }))

    await waitFor(() => {
      expect(analyticsApi.getWatchCounts).toHaveBeenCalledWith(
        'CUSTOM',
        expect.any(String),
        expect.any(String),
      )
    })
  })
})
