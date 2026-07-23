import { useState } from 'react'
import type { AnalyticsPeriod } from '../../../api/analyticsApi'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { formatUtcDate, todayIsoDate } from '../formatDuration'
import { useWatchCounts } from '../hooks/useAnalytics'
import { CountBarChart } from './TotalsSection'

export function WatchCountsSection() {
  const [period, setPeriod] = useState<AnalyticsPeriod>('MONTH')
  const [from, setFrom] = useState(todayIsoDate())
  const [to, setTo] = useState(todayIsoDate())
  const watchCounts = useWatchCounts(period, from, period === 'CUSTOM' ? to : undefined)

  return (
    <section className="analytics-section">
      <h2>Watch counts by period</h2>
      <div className="analytics-controls">
        <div className="analytics-period-toggle" role="group" aria-label="Period type">
          {(['MONTH', 'YEAR', 'CUSTOM'] as const).map((option) => (
            <button
              key={option}
              type="button"
              className={period === option ? 'analytics-period-btn analytics-period-btn--active' : 'analytics-period-btn'}
              onClick={() => setPeriod(option)}
            >
              {option === 'MONTH' ? 'Month' : option === 'YEAR' ? 'Year' : 'Custom'}
            </button>
          ))}
        </div>
        <label className="analytics-date-field">
          From
          <input
            type="date"
            value={from}
            onChange={(event) => setFrom(event.target.value)}
          />
        </label>
        {period === 'CUSTOM' && (
          <label className="analytics-date-field">
            To
            <input
              type="date"
              value={to}
              onChange={(event) => setTo(event.target.value)}
            />
          </label>
        )}
      </div>
      {watchCounts.isLoading && <p>Loading watch counts…</p>}
      {watchCounts.error && (
        <p className="analytics-error">
          {getErrorMessage(watchCounts.error, 'Failed to load watch counts')}
        </p>
      )}
      {watchCounts.data && (
        <>
          <p className="analytics-meta">
            Period: {watchCounts.data.period} · {formatUtcDate(watchCounts.data.from)} –{' '}
            {formatUtcDate(watchCounts.data.to)}
          </p>
          <CountBarChart counts={watchCounts.data.counts} />
        </>
      )}
    </section>
  )
}
