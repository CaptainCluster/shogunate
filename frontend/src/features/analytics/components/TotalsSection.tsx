import { useTranslation } from 'react-i18next'
import type { TargetTypeCounts } from '../../../api/analyticsApi'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { useAnalyticsTotals } from '../hooks/useAnalytics'

export function TotalsSection() {
  const { t } = useTranslation('analytics')
  const totals = useAnalyticsTotals()

  return (
    <section className="analytics-section">
      <h2>{t('totals.title')}</h2>
      {totals.isLoading && <p>{t('totals.loading')}</p>}
      {totals.error && (
        <p className="analytics-error">
          {getErrorMessage(totals.error, t('totals.loadFailed'))}
        </p>
      )}
      {totals.data && (
        <div className="analytics-stat-grid">
          <div className="analytics-stat-card">
            <span className="analytics-stat-value">{totals.data.counts.episodes}</span>
            <span className="analytics-stat-label">{t('totals.episodesWatched')}</span>
          </div>
          <div className="analytics-stat-card">
            <span className="analytics-stat-value">{totals.data.counts.seasons}</span>
            <span className="analytics-stat-label">{t('totals.seasonsWatched')}</span>
          </div>
          <div className="analytics-stat-card">
            <span className="analytics-stat-value">{totals.data.counts.shows}</span>
            <span className="analytics-stat-label">{t('totals.showsWatched')}</span>
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
  const { t } = useTranslation('analytics')
  const items = [
    { label: t('watchCounts.episodes'), value: counts.episodes },
    { label: t('watchCounts.seasons'), value: counts.seasons },
    { label: t('watchCounts.shows'), value: counts.shows },
  ]
  const max = Math.max(...items.map((item) => item.value), 1)

  return (
    <div className="analytics-bar-chart" aria-label={t('watchCounts.chartAria')}>
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
