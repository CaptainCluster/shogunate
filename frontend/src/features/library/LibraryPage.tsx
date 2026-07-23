import { Link } from 'react-router-dom'
import { Trans, useTranslation } from 'react-i18next'
import type { ShowSummary } from '../../api/showApi'
import { getErrorMessage } from '../../lib/getErrorMessage'
import { FavoriteSuggestionsPanel } from '../favorites/components/FavoriteSuggestionsPanel'
import { useFavorites } from '../favorites/hooks/useFavorites'
import { RemoveFromLibraryButton } from './components/RemoveFromLibraryButton'
import { useShowLibrary } from './hooks/useShowLibrary'
import { formatLibraryStatus } from './formatLibraryStatus'
import './LibraryPage.css'
import '../favorites/favorites.css'

export function LibraryPage() {
  const { t } = useTranslation('library')
  const library = useShowLibrary()
  const favorites = useFavorites()
  const favoriteShowIds = new Set(favorites.data?.map((favorite) => favorite.showId) ?? [])

  return (
    <div className="library-page">
      <h1>{t('title')}</h1>
      <p>
        <Link to="/search">{t('searchForShows')}</Link>
      </p>

      <FavoriteSuggestionsPanel />

      <section className="library-section">
        <h2>{t('inYourLibrary')}</h2>
        {library.isLoading && <p>{t('loading')}</p>}
        {library.error && (
          <p className="library-error">{getErrorMessage(library.error, t('loadFailed'))}</p>
        )}
        {library.data?.length === 0 && !library.isLoading && (
          <p>
            <Trans
              i18nKey="empty"
              ns="library"
              components={{
                link: <Link to="/search" />,
              }}
            />
          </p>
        )}
        <ul className="library-list">
          {library.data?.map((show: ShowSummary) => (
            <li key={show.id} className="library-card">
              <div className="library-card__main">
                {show.posterUrl && (
                  <img src={show.posterUrl} alt="" className="library-poster" />
                )}
                <div>
                  <h3>
                    <Link to={`/library/${show.id}`}>{show.title}</Link>
                  </h3>
                  <p className="library-meta">
                    {t('statusLabel', { status: formatLibraryStatus(show.libraryStatus) })}
                  </p>
                  <RemoveFromLibraryButton showId={show.id} showTitle={show.title}>
                    {t('remove')}
                  </RemoveFromLibraryButton>
                </div>
              </div>
              {favoriteShowIds.has(show.id) && (
                <span className="favorite-badge library-card__favorite-badge">{t('favoriteBadge')}</span>
              )}
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
