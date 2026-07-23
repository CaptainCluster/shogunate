import { cleanup, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it } from 'vitest'
import { ConfirmProvider } from '../components/ConfirmProvider'
import { useConfirm } from '../hooks/useConfirm'
import { renderWithI18n } from '../test/renderWithI18n'

afterEach(() => {
  cleanup()
})

function ConfirmHarness({ onResult }: { onResult: (value: boolean) => void }) {
  const { confirm } = useConfirm()

  return (
    <button
      type="button"
      onClick={async () => {
        const confirmed = await confirm({
          title: 'Confirm?',
          message: 'Are you sure?',
        })
        onResult(confirmed)
      }}
    >
      Open confirm
    </button>
  )
}

describe('useConfirm', () => {
  it('resolves true when confirmed', async () => {
    const user = userEvent.setup()
    let confirmed: boolean | undefined

    renderWithI18n(
      <ConfirmProvider>
        <ConfirmHarness onResult={(value) => { confirmed = value }} />
      </ConfirmProvider>,
    )

    await user.click(screen.getByRole('button', { name: 'Open confirm' }))
    await waitFor(() => expect(screen.getByRole('dialog')).toBeInTheDocument())
    await user.click(screen.getByRole('button', { name: 'Confirm' }))

    await waitFor(() => expect(confirmed).toBe(true))
  })

  it('resolves false when cancelled', async () => {
    const user = userEvent.setup()
    let confirmed: boolean | undefined

    renderWithI18n(
      <ConfirmProvider>
        <ConfirmHarness onResult={(value) => { confirmed = value }} />
      </ConfirmProvider>,
    )

    await user.click(screen.getByRole('button', { name: 'Open confirm' }))
    await waitFor(() => expect(screen.getByRole('dialog')).toBeInTheDocument())
    await user.click(screen.getByRole('button', { name: 'Cancel' }))

    await waitFor(() => expect(confirmed).toBe(false))
  })

  it('resolves false on Escape', async () => {
    const user = userEvent.setup()
    let confirmed: boolean | undefined

    renderWithI18n(
      <ConfirmProvider>
        <ConfirmHarness onResult={(value) => { confirmed = value }} />
      </ConfirmProvider>,
    )

    await user.click(screen.getByRole('button', { name: 'Open confirm' }))
    await waitFor(() => expect(screen.getByRole('dialog')).toBeInTheDocument())
    await user.keyboard('{Escape}')

    await waitFor(() => expect(confirmed).toBe(false))
  })
})
