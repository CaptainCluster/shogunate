import { Link } from 'react-router-dom'
import { Trans, useTranslation } from 'react-i18next'
import { useAuth } from '../../hooks/useAuth'
import './HomePage.css'

export function HomePage() {
  const { user, logout } = useAuth()
  const { t } = useTranslation('common')

  return (
    <section className="home-page">
      <h1>{t('brand')}</h1>
      <p className="home-page__tagline">{t('home.tagline')}</p>
      {user ? (
        <div>
          <p className="home-page__signed-in">{t('home.signedInAs', { username: user.username })}</p>
          <div className="home-page__actions">
            <Link to="/library" className="ui-button ui-button--primary">
              {t('home.browseLibrary')}
            </Link>
            <Link to="/search" className="ui-button ui-button--ghost">
              {t('home.searchShows')}
            </Link>
            <Link to="/analytics" className="ui-button ui-button--ghost">
              {t('home.viewAnalytics')}
            </Link>
          </div>
          <p className="home-page__actions">
            <button type="button" className="ui-button ui-button--ghost" onClick={logout}>
              {t('nav.logout')}
            </button>
          </p>
        </div>
      ) : (
        <p className="home-page__guest">
          <Trans
            i18nKey="home.guestPrompt"
            ns="common"
            components={{
              login: <Link to="/login" />,
              register: <Link to="/register" />,
            }}
          />
        </p>
      )}
    </section>
  )
}
