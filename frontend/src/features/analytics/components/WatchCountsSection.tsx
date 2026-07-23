import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { AnalyticsPeriod } from '../../../api/analyticsApi'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { formatUtcDate, todayIsoDate } from '../formatDuration'
import { useWatchCounts } from '../hooks/useAnalytics'
import { CountBarChart } from './TotalsSection'

const PERIOD_LABEL_KEYS: Record<AnalyticsPeriod, string> = {
  MONTH: 'watchCounts.periodMonth',
  YEAR: 'watchCounts.periodYear',
  CUSTOM: 'watchCounts.periodCustom',
}

export function WatchCountsSection() {
  const { t } = useTranslation('analytics')
  const [period, setPeriod] = useState<AnalyticsPeriod>('MONTH')
  const [from, setFrom] = useState(todayIsoDate())
  const [to, setTo] = useState(todayIsoDate())
  const watchCounts = useWatchCounts(period, from, period === 'CUSTOM' ? to : undefined)

  return (
    <section className="analytics-section">
      <h2>{t('watchCounts.title')}</h2>
      <div className="analytics-controls">
        <div className="analytics-period-toggle" role="group" aria-label={t('watchCounts.periodTypeAria')}>
          {(['MONTH', 'YEAR', 'CUSTOM'] as const).map((option) => (
            <button
              key={option}
              type="button"
              className={period === option ? 'analytics-period-btn analytics-period-btn--active' : 'analytics-period-btn'}
              onClick={() => setPeriod(option)}
            >
              {t(PERIOD_LABEL_KEYS[option])}
            </button>
          ))}
        </div>
        <label className="analytics-date-field">
          {t('watchCounts.from')}
          <input
            type="date"
            value={from}
            onChange={(event) => setFrom(event.target.value)}
          />
        </label>
        {period === 'CUSTOM' && (
          <label className="analytics-date-field">
            {t('watchCounts.to')}
            <input
              type="date"
              value={to}
              onChange={(event) => setTo(event.target.value)}
            />
          </label>
        )}
      </div>
      {watchCounts.isLoading && <p>{t('watchCounts.loading')}</p>}
      {watchCounts.error && (
        <p className="analytics-error">
          {getErrorMessage(watchCounts.error, t('watchCounts.loadFailed'))}
        </p>
      )}
      {watchCounts.data && (
        <>
          <p className="analytics-meta">
            {t('watchCounts.periodMeta', {
              period: t(PERIOD_LABEL_KEYS[watchCounts.data.period]),
              from: formatUtcDate(watchCounts.data.from),
              to: formatUtcDate(watchCounts.data.to),
            })}
          </p>
          <CountBarChart counts={watchCounts.data.counts} />
        </>
      )}
    </section>
  )
}
