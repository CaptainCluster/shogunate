import { Link, useNavigate, useParams } from 'react-router-dom'
import { getErrorMessage } from '../../lib/getErrorMessage'
import {
  useRemoveShow,
  useShowDetail,
  useUpdateLibraryStatus,
} from './hooks/useShowLibrary'
import './LibraryPage.css'

export function ShowDetailPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const show = useShowDetail(id)
  const updateStatus = useUpdateLibraryStatus(id)
  const removeShow = useRemoveShow()

  if (show.isLoading) {
    return <p>Loading show…</p>
  }

  if (show.error || !show.data) {
    return <p className="library-error">{getErrorMessage(show.error, 'Show not found')}</p>
  }

  const data = show.data

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
          <label htmlFor="library-status">Library status</label>
          <select
            id="library-status"
            value={data.libraryStatus}
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
          <h3>
            {season.name ?? `Season ${season.seasonNumber}`}
          </h3>
          <ul>
            {season.episodes.map((episode) => (
              <li key={episode.id}>
                {episode.episodeNumber}. {episode.title ?? 'Untitled'}
                {episode.airDate && ` (${episode.airDate})`}
              </li>
            ))}
          </ul>
        </section>
      ))}
    </div>
  )
}
