import { useTranslation } from 'react-i18next'
import type { Episode } from '../../../api/showApi'

interface SeasonProgressProps {
  episodes: Episode[]
}

export function SeasonProgress({ episodes }: SeasonProgressProps) {
  const { t } = useTranslation('watch')
  const watchedCount = episodes.filter((episode) => episode.watched).length
  const totalCount = episodes.length

  return (
    <span className="season-progress">
      {t('seasonProgress', { watched: watchedCount, total: totalCount })}
    </span>
  )
}
