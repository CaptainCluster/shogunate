import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ConfirmProvider } from '../../../components/ConfirmProvider'
import * as showApi from '../../../api/showApi'
import { renderWithI18n } from '../../../test/renderWithI18n'
import { RemoveFromLibraryButton } from './RemoveFromLibraryButton'

vi.mock('../../../api/showApi', () => ({
  removeShow: vi.fn(),
}))

function renderWithProviders(ui: React.ReactElement) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })

  return renderWithI18n(
    <QueryClientProvider client={queryClient}>
      <ConfirmProvider>{ui}</ConfirmProvider>
    </QueryClientProvider>,
  )
}

describe('RemoveFromLibraryButton', () => {
  it('does not remove when confirmation is cancelled', async () => {
    const user = userEvent.setup()

    const { container } = renderWithProviders(
      <RemoveFromLibraryButton showId="show-1" showTitle="Test Show">
        Remove
      </RemoveFromLibraryButton>,
    )

    await user.click(within(container).getByRole('button', { name: 'Remove' }))
    expect(screen.getByRole('dialog')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Cancel' }))
    expect(showApi.removeShow).not.toHaveBeenCalled()
  })

  it('removes the show when confirmation is accepted', async () => {
    const user = userEvent.setup()
    vi.mocked(showApi.removeShow).mockResolvedValue(undefined)

    const { container } = renderWithProviders(
      <RemoveFromLibraryButton showId="show-1" showTitle="Test Show">
        Remove
      </RemoveFromLibraryButton>,
    )

    await user.click(within(container).getByRole('button', { name: 'Remove' }))
    const dialog = screen.getByRole('dialog')
    await user.click(within(dialog).getByRole('button', { name: 'Remove' }))

    expect(showApi.removeShow).toHaveBeenCalledWith('show-1')
  })
})
