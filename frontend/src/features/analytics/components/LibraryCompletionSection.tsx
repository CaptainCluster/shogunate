import { Link } from 'react-router-dom'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { formatPercent } from '../formatDuration'
import { useLibraryCompletion } from '../hooks/useAnalytics'

export function LibraryCompletionSection() {
  const completion = useLibraryCompletion()

  return (
    <section className="analytics-section">
      <h2>Library completion</h2>
      {completion.isLoading && <p>Loading completion…</p>}
      {completion.error && (
        <p className="analytics-error">
          {getErrorMessage(completion.error, 'Failed to load library completion')}
        </p>
      )}
      {completion.data && (
        <>
          <div className="analytics-completion-summary">
            <span className="analytics-stat-value">
              {formatPercent(completion.data.overallCompletionPercent)}
            </span>
            <span className="analytics-stat-label">
              Overall · {completion.data.watchedEpisodes} / {completion.data.totalEpisodes} episodes
            </span>
            <div className="analytics-bar-track analytics-bar-track--summary">
              <div
                className="analytics-bar-fill"
                style={{ width: `${completion.data.overallCompletionPercent}%` }}
              />
            </div>
          </div>
          {completion.data.shows.length === 0 ? (
            <p>No shows with episodes in your library.</p>
          ) : (
            <ul className="analytics-completion-list">
              {completion.data.shows.map((show) => (
                <li key={show.showId} className="analytics-completion-item">
                  <div className="analytics-completion-header">
                    <Link to={`/library/${show.showId}`}>{show.title}</Link>
                    {show.fullyWatched && (
                      <span className="analytics-badge">Fully watched</span>
                    )}
                  </div>
                  <span className="analytics-completion-meta">
                    {show.watchedEpisodes} / {show.totalEpisodes} episodes ·{' '}
                    {formatPercent(show.completionPercent)}
                  </span>
                  <div className="analytics-bar-track">
                    <div
                      className="analytics-bar-fill"
                      style={{ width: `${show.completionPercent}%` }}
                    />
                  </div>
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </section>
  )
}
