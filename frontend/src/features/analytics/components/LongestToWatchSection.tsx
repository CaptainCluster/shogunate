import { Link } from 'react-router-dom'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { formatDuration, formatUtcDateTime } from '../formatDuration'
import { useLongestToWatch } from '../hooks/useAnalytics'

export function LongestToWatchSection() {
  const longest = useLongestToWatch()

  return (
    <section className="analytics-section">
      <h2>Longest time to watch</h2>
      {longest.isLoading && <p>Loading ranking…</p>}
      {longest.error && (
        <p className="analytics-error">
          {getErrorMessage(longest.error, 'Failed to load longest-to-watch ranking')}
        </p>
      )}
      {longest.data?.length === 0 && !longest.isLoading && (
        <p>No shows with watch history yet.</p>
      )}
      {longest.data && longest.data.length > 0 && (
        <ol className="analytics-ranked-list">
          {longest.data.map((entry) => (
            <li key={entry.showId} className="analytics-ranked-item">
              <Link to={`/library/${entry.showId}`}>{entry.title}</Link>
              <span className="analytics-ranked-duration">{formatDuration(entry.durationSeconds)}</span>
              <span className="analytics-ranked-dates">
                {formatUtcDateTime(entry.firstWatchedAt)} → {formatUtcDateTime(entry.lastWatchedAt)}
              </span>
            </li>
          ))}
        </ol>
      )}
    </section>
  )
}
