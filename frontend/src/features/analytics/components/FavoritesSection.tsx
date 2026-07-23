import { Link } from 'react-router-dom'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { useShowLibrary } from '../../library/hooks/useShowLibrary'
import { useAnalyticsFavorites } from '../hooks/useAnalytics'

export function FavoritesSection() {
  const favorites = useAnalyticsFavorites()
  const library = useShowLibrary()
  const showById = new Map(library.data?.map((show) => [show.id, show]) ?? [])

  return (
    <section className="analytics-section">
      <h2>Favorites</h2>
      {favorites.isLoading && <p>Loading favorites…</p>}
      {favorites.error && (
        <p className="analytics-error">
          {getErrorMessage(favorites.error, 'Failed to load favorites')}
        </p>
      )}
      {favorites.data?.length === 0 && !favorites.isLoading && (
        <p>
          No favorites yet.{' '}
          <Link to="/library">Browse your library</Link> to add some.
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
