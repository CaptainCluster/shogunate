import { FavoritesSection } from './components/FavoritesSection'
import { LibraryCompletionSection } from './components/LibraryCompletionSection'
import { LongestToWatchSection } from './components/LongestToWatchSection'
import { PlanToWatchSection } from './components/PlanToWatchSection'
import { TotalsSection } from './components/TotalsSection'
import { WatchCountsSection } from './components/WatchCountsSection'
import { WatchStreaksSection } from './components/WatchStreaksSection'
import './analytics.css'

export function AnalyticsPage() {
  return (
    <div className="analytics-page">
      <h1>Analytics</h1>
      <p className="analytics-intro">Your personal watch statistics. All dates shown in UTC.</p>
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
