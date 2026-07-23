import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import enAnalytics from './locales/en/analytics.json'
import enAuth from './locales/en/auth.json'
import enCommon from './locales/en/common.json'
import enFavorites from './locales/en/favorites.json'
import enLibrary from './locales/en/library.json'
import enReviews from './locales/en/reviews.json'
import enWatch from './locales/en/watch.json'
import fiAnalytics from './locales/fi/analytics.json'
import fiAuth from './locales/fi/auth.json'
import fiCommon from './locales/fi/common.json'
import fiFavorites from './locales/fi/favorites.json'
import fiLibrary from './locales/fi/library.json'
import fiReviews from './locales/fi/reviews.json'
import fiWatch from './locales/fi/watch.json'

export const LOCALE_STORAGE_KEY = 'locale'
export const SUPPORTED_LOCALES = ['en', 'fi'] as const
export type SupportedLocale = (typeof SUPPORTED_LOCALES)[number]

const resources = {
  en: {
    common: enCommon,
    auth: enAuth,
    library: enLibrary,
    watch: enWatch,
    favorites: enFavorites,
    reviews: enReviews,
    analytics: enAnalytics,
  },
  fi: {
    common: fiCommon,
    auth: fiAuth,
    library: fiLibrary,
    watch: fiWatch,
    favorites: fiFavorites,
    reviews: fiReviews,
    analytics: fiAnalytics,
  },
} as const

function getStoredLocale(): SupportedLocale {
  try {
    const stored = localStorage.getItem(LOCALE_STORAGE_KEY)
    if (stored === 'en' || stored === 'fi') {
      return stored
    }
  } catch {
    // localStorage unavailable (e.g. some test environments)
  }
  return 'en'
}

function syncDocumentLocale(lng: string) {
  document.documentElement.lang = lng
  document.title = i18n.t('pageTitle', { ns: 'common' })
}

void i18n.use(initReactI18next).init({
  resources,
  lng: getStoredLocale(),
  fallbackLng: 'en',
  defaultNS: 'common',
  interpolation: {
    escapeValue: false,
  },
})

i18n.on('languageChanged', (lng) => {
  try {
    localStorage.setItem(LOCALE_STORAGE_KEY, lng)
  } catch {
    // localStorage unavailable
  }
  syncDocumentLocale(lng)
})

syncDocumentLocale(i18n.language)

export default i18n
