import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../hooks/useAuth'
import { Outlet } from 'react-router-dom'
import { LanguageSwitch } from './LanguageSwitch'
import './Layout.css'

export function Layout() {
  const { user, logout } = useAuth()
  const { t } = useTranslation('common')

  return (
    <div className="layout">
      <header className="layout-header">
        <Link to="/" className="layout-brand">
          {t('brand')}
        </Link>
        <nav className="layout-nav">
          <Link to="/">{t('nav.home')}</Link>
          {user && <Link to="/library">{t('nav.library')}</Link>}
          {user && <Link to="/search">{t('nav.search')}</Link>}
          {user && <Link to="/analytics">{t('nav.analytics')}</Link>}
          <Link to="/about">{t('nav.about')}</Link>
          <LanguageSwitch />
          {user ? (
            <>
              <span>{user.username}</span>
              <button type="button" onClick={logout}>
                {t('nav.logout')}
              </button>
            </>
          ) : (
            <>
              <Link to="/login">{t('nav.login')}</Link>
              <Link to="/register">{t('nav.register')}</Link>
            </>
          )}
        </nav>
      </header>
      <main className="layout-main">
        <Outlet />
      </main>
    </div>
  )
}
