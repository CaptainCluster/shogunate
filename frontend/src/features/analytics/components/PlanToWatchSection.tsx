import { useTranslation } from 'react-i18next'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { usePlanToWatchCount } from '../hooks/useAnalytics'

export function PlanToWatchSection() {
  const { t } = useTranslation('analytics')
  const planToWatch = usePlanToWatchCount()

  return (
    <section className="analytics-section">
      <h2>{t('planToWatch.title')}</h2>
      {planToWatch.isLoading && <p>{t('planToWatch.loading')}</p>}
      {planToWatch.error && (
        <p className="analytics-error">
          {getErrorMessage(planToWatch.error, t('planToWatch.loadFailed'))}
        </p>
      )}
      {planToWatch.data && (
        <div className="analytics-stat-card analytics-stat-card--inline">
          <span className="analytics-stat-value">{planToWatch.data.count}</span>
          <span className="analytics-stat-label">{t('planToWatch.label')}</span>
        </div>
      )}
    </section>
  )
}
