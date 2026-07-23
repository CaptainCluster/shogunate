import { Link } from 'react-router-dom'
import { Trans, useTranslation } from 'react-i18next'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { useShowLibrary } from '../../library/hooks/useShowLibrary'
import { useAnalyticsFavorites } from '../hooks/useAnalytics'

export function FavoritesSection() {
  const { t } = useTranslation('analytics')
  const favorites = useAnalyticsFavorites()
  const library = useShowLibrary()
  const showById = new Map(library.data?.map((show) => [show.id, show]) ?? [])

  return (
    <section className="analytics-section">
      <h2>{t('favorites.title')}</h2>
      {favorites.isLoading && <p>{t('favorites.loading')}</p>}
      {favorites.error && (
        <p className="analytics-error">
          {getErrorMessage(favorites.error, t('favorites.loadFailed'))}
        </p>
      )}
      {favorites.data?.length === 0 && !favorites.isLoading && (
        <p>
          <Trans
            i18nKey="favorites.empty"
            ns="analytics"
            components={{
              link: <Link to="/library" />,
            }}
          />
        </p>
      )}
      {favorites.data && favorites.data.length > 0 && (
        <ul className="analytics-list">
          {favorites.data.map((favorite) => {
            const show = showById.get(favorite.showId)
            const title = show?.title ?? favorite.showId
            return (
              <li key={favorite.id} className="analytics-list-item">
                {show?.posterUrl && (
                  <img src={show.posterUrl} alt="" className="analytics-list-poster" />
                )}
                <Link to={`/library/${favorite.showId}`}>{title}</Link>
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}
