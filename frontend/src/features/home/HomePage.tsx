import { Link } from 'react-router-dom'
import { Trans, useTranslation } from 'react-i18next'
import { useAuth } from '../../hooks/useAuth'

export function HomePage() {
  const { user, logout } = useAuth()
  const { t } = useTranslation('common')

  return (
    <section>
      <h1>{t('brand')}</h1>
      <p>{t('home.tagline')}</p>
      {user ? (
        <div>
          <p>{t('home.signedInAs', { username: user.username })}</p>
          <p>
            <Link to="/library">{t('home.browseLibrary')}</Link>
            {' · '}
            <Link to="/search">{t('home.searchShows')}</Link>
            {' · '}
            <Link to="/analytics">{t('home.viewAnalytics')}</Link>
          </p>
          <button type="button" onClick={logout}>
            {t('nav.logout')}
          </button>
        </div>
      ) : (
        <p>
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
