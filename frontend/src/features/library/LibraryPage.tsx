import { Link } from 'react-router-dom'
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
  const library = useShowLibrary()
  const favorites = useFavorites()
  const favoriteShowIds = new Set(favorites.data?.map((favorite) => favorite.showId) ?? [])

  return (
    <div className="library-page">
      <h1>Your library</h1>
      <p>
        <Link to="/search">Search for shows</Link>
      </p>

      <FavoriteSuggestionsPanel />

      <section className="library-section">
        <h2>In your library</h2>
        {library.isLoading && <p>Loading library…</p>}
        {library.error && (
          <p className="library-error">{getErrorMessage(library.error, 'Failed to load library')}</p>
        )}
        {library.data?.length === 0 && !library.isLoading && (
          <p>
            No shows yet. <Link to="/search">Search for shows</Link> to add one.
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
                  <p className="library-meta">Status: {formatLibraryStatus(show.libraryStatus)}</p>
                  <RemoveFromLibraryButton showId={show.id} showTitle={show.title}>
                    Remove
                  </RemoveFromLibraryButton>
                </div>
              </div>
              {favoriteShowIds.has(show.id) && (
                <span className="favorite-badge library-card__favorite-badge">Favorite</span>
              )}
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
