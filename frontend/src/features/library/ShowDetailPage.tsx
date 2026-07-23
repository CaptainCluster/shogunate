import { Link, useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { getErrorMessage } from '../../lib/getErrorMessage'
import { FavoriteToggle } from '../favorites/components/FavoriteToggle'
import { WatchedReviewEditor } from '../reviews/components/WatchedReviewEditor'
import { SeasonProgress } from '../watch/components/SeasonProgress'
import { WatchButtonPair } from '../watch/components/WatchButtonPair'
import { useWatchMutations } from '../watch/hooks/useWatchMutations'
import { RemoveFromLibraryButton } from './components/RemoveFromLibraryButton'
import { useShowDetail, useUpdateLibraryStatus } from './hooks/useShowLibrary'
import { formatLibraryStatus } from './formatLibraryStatus'
import './LibraryPage.css'
import '../watch/watch.css'
import '../reviews/reviews.css'
import '../favorites/favorites.css'

export function ShowDetailPage() {
  const { t } = useTranslation('library')
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const show = useShowDetail(id)
  const updateStatus = useUpdateLibraryStatus(id)
  const watchMutations = useWatchMutations(id)

  if (show.isLoading) {
    return <p>{t('detail.loading')}</p>
  }

  if (show.error || !show.data) {
    return <p className="library-error">{getErrorMessage(show.error, t('detail.notFound'))}</p>
  }

  const data = show.data
  const totalEpisodes = data.seasons.reduce(
    (count, season) => count + season.episodes.length,
    0,
  )
  const seasonCount = data.seasons.length

  const mutationProps = {
    markEpisode: watchMutations.markEpisode,
    unmarkEpisode: watchMutations.unmarkEpisode,
    markSeason: watchMutations.markSeason,
    unmarkSeason: watchMutations.unmarkSeason,
    markShow: watchMutations.markShow,
    unmarkShow: watchMutations.unmarkShow,
    pendingAction: watchMutations.pendingAction,
  }

  return (
    <div className="library-page">
      <nav className="library-back-nav">
        <Link to="/library" className="library-back-link">
          {t('backToLibrary')}
        </Link>
      </nav>

      <div className="library-detail-hero">
        {data.posterUrl && (
          <figure className="library-detail-poster-frame">
            <img src={data.posterUrl} alt="" className="library-detail-poster" />
          </figure>
        )}
        <div className="library-detail-info">
          <h1>{data.title}</h1>
          {data.firstAirDate && (
            <p className="library-detail-meta">{t('detail.premiered', { date: data.firstAirDate })}</p>
          )}
          {data.overview && <p className="library-detail-overview">{data.overview}</p>}
          {data.tvmazeUrl && (
            <p className="library-detail-meta">
              <a href={data.tvmazeUrl} target="_blank" rel="noreferrer">
                {t('detail.viewOnTvmaze')}
              </a>
            </p>
          )}
        </div>
      </div>

      <section className="library-detail-actions" aria-label={t('detail.actionsAria')}>
        <div className="library-detail-watch-row">
          <div className="library-detail-watch-row__controls">
            <WatchButtonPair
              targetType="SHOW"
              targetId={data.id}
              watched={data.watched}
              watchedAt={data.watchedAt}
              label={data.title}
              episodeCount={totalEpisodes}
              seasonCount={seasonCount}
              mutations={mutationProps}
            />
          </div>
          <div className="library-detail-watch-row__status">
            <span className="library-detail-status__label" id="library-status-label">
              {t('detail.libraryStatus')}
            </span>
            {data.libraryStatus === 'WATCHED' ? (
              <span
                id="library-status"
                className="library-detail-status__value library-detail-status__value--watched"
                aria-labelledby="library-status-label"
              >
                {formatLibraryStatus(data.libraryStatus)}
              </span>
            ) : (
              <select
                id="library-status"
                className="ui-select library-detail-status__select"
                aria-labelledby="library-status-label"
                value={data.libraryStatus}
                disabled={watchMutations.isPending || updateStatus.isPending}
                onChange={(event) =>
                  updateStatus.mutate(event.target.value as 'NONE' | 'PLAN_TO_WATCH')
                }
              >
                <option value="NONE">{formatLibraryStatus('NONE')}</option>
                <option value="PLAN_TO_WATCH">{formatLibraryStatus('PLAN_TO_WATCH')}</option>
              </select>
            )}
          </div>
        </div>
        {watchMutations.error && (
          <p className="library-error watch-error">
            {getErrorMessage(watchMutations.error, t('detail.watchUpdateFailed'))}
          </p>
        )}
        <FavoriteToggle showId={data.id} />
        <WatchedReviewEditor
          watched={data.watched}
          className="show-review"
          targetType="SHOW"
          targetId={data.id}
          label={t('detail.rateShow', { title: data.title })}
        />
        <RemoveFromLibraryButton
          showId={id}
          showTitle={data.title}
          onSuccess={() => navigate('/library')}
        />
      </section>

      <h2>{t('detail.seasons')}</h2>
      {data.seasons.map((season) => {
        const seasonTitle = season.name ?? t('detail.seasonNumber', { number: season.seasonNumber })

        return (
          <section key={season.id} className="season-block">
            <div className="season-header-row">
              <h3>{seasonTitle}</h3>
              <SeasonProgress episodes={season.episodes} />
              <WatchButtonPair
                targetType="SEASON"
                targetId={season.id}
                watched={season.watched}
                watchedAt={season.watchedAt}
                label={seasonTitle}
                episodeCount={season.episodes.length}
                mutations={mutationProps}
              />
            </div>
            <WatchedReviewEditor
              watched={season.watched}
              className="season-review"
              targetType="SEASON"
              targetId={season.id}
              label={t('detail.rateSeason', { title: seasonTitle })}
            />
            <ul>
              {season.episodes.map((episode) => (
                <li
                  key={episode.id}
                  className={`episode-row${episode.watched ? ' episode-row--watched' : ''}`}
                >
                  <div className="episode-row__content">
                    <div className="episode-title">
                      {episode.episodeNumber}. {episode.title ?? t('detail.untitled')}
                      {episode.airDate && ` (${episode.airDate})`}
                    </div>
                    <WatchedReviewEditor
                      watched={episode.watched}
                      compact
                      collapseExistingReview
                      targetType="EPISODE"
                      targetId={episode.id}
                      label={t('detail.rateEpisode', { number: episode.episodeNumber })}
                    />
                  </div>
                  <div className="episode-row__watch">
                    <WatchButtonPair
                      targetType="EPISODE"
                      targetId={episode.id}
                      watched={episode.watched}
                      watchedAt={episode.watchedAt}
                      label={t('detail.episodeLabel', { number: episode.episodeNumber })}
                      mutations={mutationProps}
                    />
                  </div>
                </li>
              ))}
            </ul>
          </section>
        )
      })}
    </div>
  )
}
