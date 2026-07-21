import { Link, useNavigate, useParams } from 'react-router-dom'
import { getErrorMessage } from '../../lib/getErrorMessage'
import { SeasonProgress } from '../watch/components/SeasonProgress'
import { WatchButtonPair } from '../watch/components/WatchButtonPair'
import { useWatchMutations } from '../watch/hooks/useWatchMutations'
import {
  useRemoveShow,
  useShowDetail,
  useUpdateLibraryStatus,
} from './hooks/useShowLibrary'
import './LibraryPage.css'
import '../watch/watch.css'

export function ShowDetailPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const show = useShowDetail(id)
  const updateStatus = useUpdateLibraryStatus(id)
  const removeShow = useRemoveShow()
  const watchMutations = useWatchMutations(id)

  if (show.isLoading) {
    return <p>Loading show…</p>
  }

  if (show.error || !show.data) {
    return <p className="library-error">{getErrorMessage(show.error, 'Show not found')}</p>
  }

  const data = show.data
  const totalEpisodes = data.seasons.reduce(
    (count, season) => count + season.episodes.length,
    0,
  )
  const seasonCount = data.seasons.length

  const mutationProps = {
    markEpisode: watchMutations.markEpisode,
    unmarkEpisode: watchMutations.unmarkEpisode,
    markSeason: watchMutations.markSeason,
    unmarkSeason: watchMutations.unmarkSeason,
    markShow: watchMutations.markShow,
    unmarkShow: watchMutations.unmarkShow,
    pendingAction: watchMutations.pendingAction,
  }

  function handleRemove() {
    removeShow.mutate(id, {
      onSuccess: () => navigate('/library'),
    })
  }

  return (
    <div className="library-page">
      <p>
        <Link to="/library">← Back to library</Link>
      </p>

      <div className="library-detail-header">
        {data.posterUrl && (
          <img src={data.posterUrl} alt="" className="library-detail-poster" />
        )}
        <div>
          <h1>{data.title}</h1>
          {data.firstAirDate && <p>Premiered: {data.firstAirDate}</p>}
          {data.overview && <p>{data.overview}</p>}
          {data.tvmazeUrl && (
            <p>
              <a href={data.tvmazeUrl} target="_blank" rel="noreferrer">
                View on TVmaze
              </a>
            </p>
          )}
          <div className="show-watch-controls">
            <WatchButtonPair
              targetType="SHOW"
              targetId={data.id}
              watched={data.watched}
              watchedAt={data.watchedAt}
              label={data.title}
              episodeCount={totalEpisodes}
              seasonCount={seasonCount}
              mutations={mutationProps}
            />
          </div>
          {watchMutations.error && (
            <p className="library-error watch-error">
              {getErrorMessage(watchMutations.error, 'Watch update failed')}
            </p>
          )}
          <label htmlFor="library-status">Library status</label>
          <select
            id="library-status"
            value={data.libraryStatus}
            disabled={watchMutations.isPending}
            onChange={(event) =>
              updateStatus.mutate(event.target.value as 'NONE' | 'PLAN_TO_WATCH')
            }
          >
            <option value="NONE">None</option>
            <option value="PLAN_TO_WATCH">Plan to Watch</option>
          </select>
          <p>
            <button type="button" onClick={handleRemove} disabled={removeShow.isPending}>
              Remove from library
            </button>
          </p>
        </div>
      </div>

      <h2>Seasons</h2>
      {data.seasons.map((season) => (
        <section key={season.id} className="season-block">
          <div className="season-header-row">
            <h3>{season.name ?? `Season ${season.seasonNumber}`}</h3>
            <SeasonProgress episodes={season.episodes} />
            <WatchButtonPair
              targetType="SEASON"
              targetId={season.id}
              watched={season.watched}
              watchedAt={season.watchedAt}
              label={season.name ?? `Season ${season.seasonNumber}`}
              episodeCount={season.episodes.length}
              mutations={mutationProps}
            />
          </div>
          <ul>
            {season.episodes.map((episode) => (
              <li
                key={episode.id}
                className={`episode-row${episode.watched ? ' episode-row--watched' : ''}`}
              >
                <span className="episode-title">
                  {episode.episodeNumber}. {episode.title ?? 'Untitled'}
                  {episode.airDate && ` (${episode.airDate})`}
                </span>
                <WatchButtonPair
                  targetType="EPISODE"
                  targetId={episode.id}
                  watched={episode.watched}
                  watchedAt={episode.watchedAt}
                  label={`Episode ${episode.episodeNumber}`}
                  mutations={mutationProps}
                />
              </li>
            ))}
          </ul>
        </section>
      ))}
    </div>
  )
}
