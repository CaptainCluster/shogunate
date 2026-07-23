import { getErrorMessage } from '../../../lib/getErrorMessage'
import { usePlanToWatchCount } from '../hooks/useAnalytics'

export function PlanToWatchSection() {
  const planToWatch = usePlanToWatchCount()

  return (
    <section className="analytics-section">
      <h2>Plan to watch</h2>
      {planToWatch.isLoading && <p>Loading plan-to-watch count…</p>}
      {planToWatch.error && (
        <p className="analytics-error">
          {getErrorMessage(planToWatch.error, 'Failed to load plan-to-watch count')}
        </p>
      )}
      {planToWatch.data && (
        <div className="analytics-stat-card analytics-stat-card--inline">
          <span className="analytics-stat-value">{planToWatch.data.count}</span>
          <span className="analytics-stat-label">Shows flagged plan to watch</span>
        </div>
      )}
    </section>
  )
}
