import { useTranslation } from 'react-i18next'
import { useConfirm } from '../../../hooks/useConfirm'
import type { WatchTargetType } from '../watchKeys'
import { useWatchMutations, type PendingWatchAction } from '../hooks/useWatchMutations'

export interface WatchMutationsApi {
  markEpisode: Pick<ReturnType<typeof useWatchMutations>['markEpisode'], 'mutate'>
  unmarkEpisode: Pick<ReturnType<typeof useWatchMutations>['unmarkEpisode'], 'mutate'>
  markSeason: Pick<ReturnType<typeof useWatchMutations>['markSeason'], 'mutate'>
  unmarkSeason: Pick<ReturnType<typeof useWatchMutations>['unmarkSeason'], 'mutate'>
  markShow: Pick<ReturnType<typeof useWatchMutations>['markShow'], 'mutate'>
  unmarkShow: Pick<ReturnType<typeof useWatchMutations>['unmarkShow'], 'mutate'>
  pendingAction: PendingWatchAction | null
}

interface WatchButtonPairProps {
  targetType: WatchTargetType
  targetId: string
  watched: boolean
  watchedAt: string | null
  label: string
  episodeCount?: number
  seasonCount?: number
  mutations: WatchMutationsApi
}

function formatWatchedAt(watchedAt: string | null, locale: string) {
  if (!watchedAt) {
    return null
  }
  return new Intl.DateTimeFormat(locale, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(watchedAt))
}

export function WatchButtonPair({
  targetType,
  targetId,
  watched,
  watchedAt,
  label,
  episodeCount = 0,
  seasonCount = 0,
  mutations,
}: WatchButtonPairProps) {
  const { t, i18n } = useTranslation('watch')
  const { confirm } = useConfirm()
  const {
    markEpisode,
    unmarkEpisode,
    markSeason,
    unmarkSeason,
    markShow,
    unmarkShow,
    pendingAction,
  } = mutations

  const isThisPending =
    pendingAction?.targetType === targetType && pendingAction.targetId === targetId

  function handleMark() {
    switch (targetType) {
      case 'EPISODE':
        markEpisode.mutate(targetId)
        break
      case 'SEASON':
        markSeason.mutate(targetId)
        break
      case 'SHOW':
        markShow.mutate(targetId)
        break
    }
  }

  async function handleUnmark() {
    if (targetType === 'EPISODE') {
      unmarkEpisode.mutate(targetId)
      return
    }

    const confirmed = await confirm({
      title: targetType === 'SEASON' ? t('unmarkSeason.title') : t('unmarkShow.title'),
      message:
        targetType === 'SEASON'
          ? t('unmarkSeason.message', { episodeCount })
          : t('unmarkShow.message', { seasonCount, episodeCount }),
      confirmLabel:
        targetType === 'SEASON' ? t('unmarkSeason.confirm') : t('unmarkShow.confirm'),
    })

    if (!confirmed) {
      return
    }

    if (targetType === 'SEASON') {
      unmarkSeason.mutate(targetId)
    } else {
      unmarkShow.mutate(targetId)
    }
  }

  const formattedWatchedAt = formatWatchedAt(watchedAt, i18n.language)

  return (
    <span className="watch-controls">
      {watched ? (
        <>
          {formattedWatchedAt && (
            <span className="watch-timestamp">
              {t('watchedAt', { date: formattedWatchedAt })}
            </span>
          )}
          <button
            type="button"
            className="watch-button watch-button-unmark"
            aria-label={t('unmarkAriaLabel', { label })}
            disabled={isThisPending}
            onClick={() => void handleUnmark()}
          >
            {t('unmark')}
          </button>
        </>
      ) : (
        <button
          type="button"
          className="watch-button watch-button-mark"
          aria-label={t('markAriaLabel', { label })}
          disabled={isThisPending}
          onClick={handleMark}
        >
          {t('markWatched')}
        </button>
      )}
    </span>
  )
}
