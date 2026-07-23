import { useTranslation } from 'react-i18next'
import type { SupportedLocale } from '../i18n/config'
import './LanguageSwitch.css'

export function LanguageSwitch() {
  const { i18n, t } = useTranslation('common')
  const current = i18n.language as SupportedLocale

  function setLocale(locale: SupportedLocale) {
    if (locale !== current) {
      void i18n.changeLanguage(locale)
    }
  }

  return (
    <div className="language-switch" role="group" aria-label={t('languageSwitch.ariaLabel')}>
      {(['en', 'fi'] as const).map((locale) => (
        <button
          key={locale}
          type="button"
          className={
            current === locale
              ? 'language-switch__btn language-switch__btn--active'
              : 'language-switch__btn'
          }
          aria-pressed={current === locale}
          onClick={() => setLocale(locale)}
        >
          {t(`languageSwitch.${locale}`)}
        </button>
      ))}
    </div>
  )
}
