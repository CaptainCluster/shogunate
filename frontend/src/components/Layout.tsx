import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../hooks/useAuth'
import { Outlet } from 'react-router-dom'
import { LanguageSwitch } from './LanguageSwitch'
import './Layout.css'

export function Layout() {
  const { user, logout } = useAuth()
  const { t } = useTranslation('common')
  const [navOpen, setNavOpen] = useState(false)

  function closeNav() {
    setNavOpen(false)
  }

  return (
    <div className="layout">
      <header className="layout-header">
        <div className="layout-header__bar">
          <Link to="/" className="layout-brand" onClick={closeNav}>
            {t('brand')}
          </Link>
          <button
            type="button"
            className="layout-nav-toggle"
            aria-expanded={navOpen}
            aria-controls="layout-nav"
            aria-label={t('nav.menuToggle')}
            onClick={() => setNavOpen((open) => !open)}
          >
            <span className="layout-nav-toggle__bar" aria-hidden="true" />
            <span className="layout-nav-toggle__bar" aria-hidden="true" />
            <span className="layout-nav-toggle__bar" aria-hidden="true" />
          </button>
        </div>
        <nav
          id="layout-nav"
          className={`layout-nav${navOpen ? ' layout-nav--open' : ''}`}
        >
          <Link to="/" onClick={closeNav}>
            {t('nav.home')}
          </Link>
          {user && (
            <Link to="/library" onClick={closeNav}>
              {t('nav.library')}
            </Link>
          )}
          {user && (
            <Link to="/search" onClick={closeNav}>
              {t('nav.search')}
            </Link>
          )}
          {user && (
            <Link to="/analytics" onClick={closeNav}>
              {t('nav.analytics')}
            </Link>
          )}
          <Link to="/about" onClick={closeNav}>
            {t('nav.about')}
          </Link>
          <LanguageSwitch />
          {user ? (
            <>
              <span className="layout-nav__user">{user.username}</span>
              <button type="button" className="ui-button ui-button--ghost" onClick={logout}>
                {t('nav.logout')}
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="ui-button ui-button--ghost" onClick={closeNav}>
                {t('nav.login')}
              </Link>
              <Link to="/register" className="ui-button ui-button--primary" onClick={closeNav}>
                {t('nav.register')}
              </Link>
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
