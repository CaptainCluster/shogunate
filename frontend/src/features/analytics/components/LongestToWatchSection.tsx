import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { formatDuration, formatUtcDateTime } from '../formatDuration'
import { useLongestToWatch } from '../hooks/useAnalytics'

export function LongestToWatchSection() {
  const { t } = useTranslation('analytics')
  const longest = useLongestToWatch()

  return (
    <section className="analytics-section">
      <h2>{t('longestToWatch.title')}</h2>
      {longest.isLoading && <p>{t('longestToWatch.loading')}</p>}
      {longest.error && (
        <p className="analytics-error">
          {getErrorMessage(longest.error, t('longestToWatch.loadFailed'))}
        </p>
      )}
      {longest.data?.length === 0 && !longest.isLoading && (
        <p>{t('longestToWatch.empty')}</p>
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
