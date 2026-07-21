import type { Episode } from '../../../api/showApi'

interface SeasonProgressProps {
  episodes: Episode[]
}

export function SeasonProgress({ episodes }: SeasonProgressProps) {
  const watchedCount = episodes.filter((episode) => episode.watched).length
  const totalCount = episodes.length

  return (
    <span className="season-progress">
      {watchedCount}/{totalCount} episodes watched
    </span>
  )
}
