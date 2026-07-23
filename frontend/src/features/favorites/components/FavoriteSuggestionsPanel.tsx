import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { ShowSummary } from '../../../api/showApi'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { useShowLibrary } from '../../library/hooks/useShowLibrary'
import { useFavoriteMutations } from '../hooks/useFavoriteMutations'
import { useFavoriteSuggestions } from '../hooks/useFavorites'
import '../favorites.css'

interface SuggestionRowProps {
  show: ShowSummary
}

function SuggestionRow({ show }: SuggestionRowProps) {
  const { t } = useTranslation('favorites')
  const mutations = useFavoriteMutations(show.id)

  return (
    <li className="favorites-panel__row">
      <div>
        <Link to={`/library/${show.id}`}>{show.title}</Link>
      </div>
      <button
        type="button"
        className="ui-button ui-button--ghost"
        disabled={mutations.isPending}
        onClick={() => mutations.addFavorite.mutate()}
      >
        {t('addToFavorites')}
      </button>
      {mutations.error && (
        <p className="library-error">{getErrorMessage(mutations.error, t('addFailed'))}</p>
      )}
    </li>
  )
}

export function FavoriteSuggestionsPanel() {
  const { t } = useTranslation('favorites')
  const suggestions = useFavoriteSuggestions()
  const library = useShowLibrary()

  if (suggestions.isLoading || library.isLoading) {
    return null
  }

  if (suggestions.error) {
    return (
      <section className="library-section favorites-panel">
        <h2>{t('suggestions.title')}</h2>
        <p className="library-error">
          {getErrorMessage(suggestions.error, t('suggestions.loadFailed'))}
        </p>
      </section>
    )
  }

  const libraryById = new Map(library.data?.map((show) => [show.id, show]) ?? [])
  const suggestedShows =
    suggestions.data
      ?.map((suggestion) => libraryById.get(suggestion.showId))
      .filter((show): show is ShowSummary => show !== undefined) ?? []

  if (suggestedShows.length === 0) {
    return null
  }

  return (
    <section className="library-section favorites-panel">
      <h2>{t('suggestions.title')}</h2>
      <p className="favorites-panel__meta">{t('suggestions.description')}</p>
      <ul className="favorites-panel__list">
        {suggestedShows.map((show) => (
          <SuggestionRow key={show.id} show={show} />
        ))}
      </ul>
    </section>
  )
}
