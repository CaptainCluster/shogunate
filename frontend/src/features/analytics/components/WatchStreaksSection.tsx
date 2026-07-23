import { getErrorMessage } from '../../../lib/getErrorMessage'
import { formatUtcDate } from '../formatDuration'
import { useWatchStreaks } from '../hooks/useAnalytics'

export function WatchStreaksSection() {
  const streaks = useWatchStreaks()

  return (
    <section className="analytics-section">
      <h2>Watch streaks</h2>
      {streaks.isLoading && <p>Loading streaks…</p>}
      {streaks.error && (
        <p className="analytics-error">
          {getErrorMessage(streaks.error, 'Failed to load watch streaks')}
        </p>
      )}
      {streaks.data && (
        <div className="analytics-stat-grid">
          <div className="analytics-stat-card">
            <span className="analytics-stat-value">{streaks.data.currentStreakDays}</span>
            <span className="analytics-stat-label">Current streak (days)</span>
            {streaks.data.currentStreakDays > 0 && streaks.data.currentStreakStartDate && (
              <span className="analytics-stat-meta">
                Since {formatUtcDate(streaks.data.currentStreakStartDate)}
              </span>
            )}
          </div>
          <div className="analytics-stat-card">
            <span className="analytics-stat-value">{streaks.data.longestStreakDays}</span>
            <span className="analytics-stat-label">Longest streak (days)</span>
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
