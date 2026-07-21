import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ConfirmProvider } from '../../../components/ConfirmProvider'
import * as watchApi from '../../../api/watchApi'
import { WatchButtonPair, type WatchMutationsApi } from './WatchButtonPair'

vi.mock('../../../api/watchApi', () => ({
  markEpisodeWatched: vi.fn(),
  unmarkEpisodeWatched: vi.fn(),
  markSeasonWatched: vi.fn(),
  unmarkSeasonWatched: vi.fn(),
  markShowWatched: vi.fn(),
  unmarkShowWatched: vi.fn(),
}))

const mutations: WatchMutationsApi = {
  markEpisode: { mutate: vi.fn() },
  unmarkEpisode: { mutate: vi.fn() },
  markSeason: { mutate: vi.fn() },
  unmarkSeason: { mutate: vi.fn() },
  markShow: { mutate: vi.fn() },
  unmarkShow: { mutate: vi.fn() },
  pendingAction: null,
}

function renderWithProviders(ui: React.ReactElement) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })

  return render(
    <QueryClientProvider client={queryClient}>
      <ConfirmProvider>{ui}</ConfirmProvider>
    </QueryClientProvider>,
  )
}

describe('WatchButtonPair', () => {
  it('unmarks episode immediately without confirmation', async () => {
    const user = userEvent.setup()
    vi.mocked(watchApi.unmarkEpisodeWatched).mockResolvedValue(undefined)

    renderWithProviders(
      <WatchButtonPair
        targetType="EPISODE"
        targetId="episode-1"
        watched
        watchedAt="2024-01-01T12:00:00Z"
        label="Episode 1"
        mutations={mutations}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Unmark Episode 1 as watched' }))

    expect(mutations.unmarkEpisode.mutate).toHaveBeenCalledWith('episode-1')
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('shows confirmation before unmarking season', async () => {
    const user = userEvent.setup()

    renderWithProviders(
      <WatchButtonPair
        targetType="SEASON"
        targetId="season-1"
        watched
        watchedAt="2024-01-01T12:00:00Z"
        label="Season 1"
        episodeCount={10}
        mutations={mutations}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Unmark Season 1 as watched' }))

    expect(screen.getByRole('dialog')).toBeInTheDocument()
    expect(mutations.unmarkSeason.mutate).not.toHaveBeenCalled()

    await user.click(screen.getByRole('button', { name: 'Unmark' }))

    expect(mutations.unmarkSeason.mutate).toHaveBeenCalledWith('season-1')
  })
})
