import { render, type RenderOptions } from '@testing-library/react'
import { I18nextProvider } from 'react-i18next'
import i18n from '../i18n/config'
import type { SupportedLocale } from '../i18n/config'

interface RenderWithI18nOptions extends RenderOptions {
  lng?: SupportedLocale
}

export function renderWithI18n(ui: React.ReactElement, options: RenderWithI18nOptions = {}) {
  const { lng = 'en', ...renderOptions } = options

  if (i18n.language !== lng) {
    void i18n.changeLanguage(lng)
  }

  return render(<I18nextProvider i18n={i18n}>{ui}</I18nextProvider>, renderOptions)
}

export { i18n }
