import { useTranslation } from 'react-i18next'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { useFavoriteMutations } from '../hooks/useFavoriteMutations'
import { useFavoriteStatus } from '../hooks/useFavorites'
import '../favorites.css'

interface FavoriteToggleProps {
  showId: string
}

export function FavoriteToggle({ showId }: FavoriteToggleProps) {
  const { t } = useTranslation('favorites')
  const status = useFavoriteStatus(showId)
  const mutations = useFavoriteMutations(showId)

  if (status.isLoading) {
    return <p className="favorite-controls">{t('loading')}</p>
  }

  if (status.error || !status.data) {
    return (
      <p className="library-error">
        {getErrorMessage(status.error, t('loadStatusFailed'))}
      </p>
    )
  }

  const { isFavorite, isSuggested } = status.data
  const showSuggestedBadge = isSuggested && !isFavorite

  function handleToggle() {
    if (isFavorite) {
      mutations.removeFavorite.mutate()
      return
    }
    mutations.addFavorite.mutate()
  }

  return (
    <div className="favorite-controls">
      {showSuggestedBadge && (
        <span className="favorite-badge" aria-label={t('suggestedBadgeAria')}>
          {t('suggestedBadge')}
        </span>
      )}
      <button
        type="button"
        className={isFavorite ? 'favorite-toggle--active' : undefined}
        disabled={mutations.isPending}
        aria-pressed={isFavorite}
        onClick={handleToggle}
      >
        {isFavorite ? t('removeFromFavorites') : t('addToFavorites')}
      </button>
      {mutations.error && (
        <p className="library-error">{getErrorMessage(mutations.error, t('updateFailed'))}</p>
      )}
    </div>
  )
}
