import { useTranslation } from 'react-i18next'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { formatUtcDate } from '../formatDuration'
import { useWatchStreaks } from '../hooks/useAnalytics'

export function WatchStreaksSection() {
  const { t } = useTranslation('analytics')
  const streaks = useWatchStreaks()

  return (
    <section className="analytics-section">
      <h2>{t('streaks.title')}</h2>
      {streaks.isLoading && <p>{t('streaks.loading')}</p>}
      {streaks.error && (
        <p className="analytics-error">
          {getErrorMessage(streaks.error, t('streaks.loadFailed'))}
        </p>
      )}
      {streaks.data && (
        <div className="analytics-stat-grid">
          <div className="analytics-stat-card">
            <span className="analytics-stat-value">{streaks.data.currentStreakDays}</span>
            <span className="analytics-stat-label">{t('streaks.currentStreak')}</span>
            {streaks.data.currentStreakDays > 0 && streaks.data.currentStreakStartDate && (
              <span className="analytics-stat-meta">
                {t('streaks.since', {
                  date: formatUtcDate(streaks.data.currentStreakStartDate),
                })}
              </span>
            )}
          </div>
          <div className="analytics-stat-card">
            <span className="analytics-stat-value">{streaks.data.longestStreakDays}</span>
            <span className="analytics-stat-label">{t('streaks.longestStreak')}</span>
            {streaks.data.longestStreakDays > 0 &&
              streaks.data.longestStreakStartDate &&
              streaks.data.longestStreakEndDate && (
                <span className="analytics-stat-meta">
                  {formatUtcDate(streaks.data.longestStreakStartDate)} –{' '}
                  {formatUtcDate(streaks.data.longestStreakEndDate)}
                </span>
              )}
          </div>
        </div>
      )}
    </section>
  )
}
