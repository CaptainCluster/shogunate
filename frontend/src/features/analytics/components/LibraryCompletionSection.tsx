import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { formatPercent } from '../formatDuration'
import { useLibraryCompletion } from '../hooks/useAnalytics'

export function LibraryCompletionSection() {
  const { t } = useTranslation('analytics')
  const completion = useLibraryCompletion()

  return (
    <section className="analytics-section">
      <h2>{t('completion.title')}</h2>
      {completion.isLoading && <p>{t('completion.loading')}</p>}
      {completion.error && (
        <p className="analytics-error">
          {getErrorMessage(completion.error, t('completion.loadFailed'))}
        </p>
      )}
      {completion.data && (
        <>
          <div className="analytics-completion-summary">
            <span className="analytics-stat-value">
              {formatPercent(completion.data.overallCompletionPercent)}
            </span>
            <span className="analytics-stat-label">
              {t('completion.overall', {
                watched: completion.data.watchedEpisodes,
                total: completion.data.totalEpisodes,
              })}
            </span>
            <div className="analytics-bar-track analytics-bar-track--summary">
              <div
                className="analytics-bar-fill"
                style={{ width: `${completion.data.overallCompletionPercent}%` }}
              />
            </div>
          </div>
          {completion.data.shows.length === 0 ? (
            <p>{t('completion.empty')}</p>
          ) : (
            <ul className="analytics-completion-list">
              {completion.data.shows.map((show) => (
                <li key={show.showId} className="analytics-completion-item">
                  <div className="analytics-completion-header">
                    <Link to={`/library/${show.showId}`}>{show.title}</Link>
                    {show.fullyWatched && (
                      <span className="analytics-badge">{t('completion.fullyWatched')}</span>
                    )}
                  </div>
                  <span className="analytics-completion-meta">
                    {t('completion.showMeta', {
                      watched: show.watchedEpisodes,
                      total: show.totalEpisodes,
                      percent: formatPercent(show.completionPercent),
                    })}
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
