import type { TargetTypeCounts } from '../../../api/analyticsApi'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { useAnalyticsTotals } from '../hooks/useAnalytics'

export function TotalsSection() {
  const totals = useAnalyticsTotals()

  return (
    <section className="analytics-section">
      <h2>All-time totals</h2>
      {totals.isLoading && <p>Loading totals…</p>}
      {totals.error && (
        <p className="analytics-error">
          {getErrorMessage(totals.error, 'Failed to load totals')}
        </p>
      )}
      {totals.data && (
        <div className="analytics-stat-grid">
          <div className="analytics-stat-card">
            <span className="analytics-stat-value">{totals.data.counts.episodes}</span>
            <span className="analytics-stat-label">Episodes watched</span>
          </div>
          <div className="analytics-stat-card">
            <span className="analytics-stat-value">{totals.data.counts.seasons}</span>
            <span className="analytics-stat-label">Seasons watched</span>
          </div>
          <div className="analytics-stat-card">
            <span className="analytics-stat-value">{totals.data.counts.shows}</span>
            <span className="analytics-stat-label">Shows watched</span>
          </div>
        </div>
      )}
    </section>
  )
}

interface CountBarChartProps {
  counts: TargetTypeCounts
}

export function CountBarChart({ counts }: CountBarChartProps) {
  const items = [
    { label: 'Episodes', value: counts.episodes },
    { label: 'Seasons', value: counts.seasons },
    { label: 'Shows', value: counts.shows },
  ]
  const max = Math.max(...items.map((item) => item.value), 1)

  return (
    <div className="analytics-bar-chart" aria-label="Watch counts by target type">
      {items.map((item) => (
        <div key={item.label} className="analytics-bar-row">
          <span className="analytics-bar-label">{item.label}</span>
          <div className="analytics-bar-track">
            <div
              className="analytics-bar-fill"
              style={{ width: `${(item.value / max) * 100}%` }}
            />
          </div>
          <span className="analytics-bar-value">{item.value}</span>
        </div>
      ))}
    </div>
  )
}
