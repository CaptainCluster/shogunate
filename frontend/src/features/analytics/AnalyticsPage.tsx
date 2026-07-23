import { useTranslation } from 'react-i18next'
import { FavoritesSection } from './components/FavoritesSection'
import { LibraryCompletionSection } from './components/LibraryCompletionSection'
import { LongestToWatchSection } from './components/LongestToWatchSection'
import { PlanToWatchSection } from './components/PlanToWatchSection'
import { TotalsSection } from './components/TotalsSection'
import { WatchCountsSection } from './components/WatchCountsSection'
import { WatchStreaksSection } from './components/WatchStreaksSection'
import './analytics.css'

export function AnalyticsPage() {
  const { t } = useTranslation('analytics')

  return (
    <div className="analytics-page">
      <h1>{t('title')}</h1>
      <p className="analytics-intro">{t('intro')}</p>
      <TotalsSection />
      <WatchCountsSection />
      <LongestToWatchSection />
      <FavoritesSection />
      <WatchStreaksSection />
      <LibraryCompletionSection />
      <PlanToWatchSection />
    </div>
  )
}
