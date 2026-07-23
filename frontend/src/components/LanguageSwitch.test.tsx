import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it } from 'vitest'
import { LOCALE_STORAGE_KEY } from '../i18n/config'
import { LanguageSwitch } from './LanguageSwitch'
import { renderWithI18n } from '../test/renderWithI18n'

afterEach(() => {
  localStorage.removeItem(LOCALE_STORAGE_KEY)
})

describe('LanguageSwitch', () => {
  it('switches to Finnish and persists locale', async () => {
    const user = userEvent.setup()

    renderWithI18n(<LanguageSwitch />)

    await user.click(screen.getByRole('button', { name: 'FI' }))

    expect(localStorage.getItem(LOCALE_STORAGE_KEY)).toBe('fi')
    expect(document.documentElement.lang).toBe('fi')
  })

  it('renders Finnish smoke text when locale is fi', () => {
    renderWithI18n(<h1>Kirjastosi</h1>, { lng: 'fi' })
    expect(screen.getByRole('heading', { name: 'Kirjastosi' })).toBeInTheDocument()
  })
})
