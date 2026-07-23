import i18n from '../../i18n/config'

export function formatDuration(seconds: number): string {
  if (seconds <= 0) {
    return i18n.t('duration.zero', { ns: 'analytics' })
  }

  const days = Math.floor(seconds / 86_400)
  const hours = Math.floor((seconds % 86_400) / 3_600)
  const minutes = Math.floor((seconds % 3_600) / 60)

  const parts: string[] = []
  if (days > 0) {
    parts.push(i18n.t('duration.days', { ns: 'analytics', count: days }))
  }
  if (hours > 0) {
    parts.push(i18n.t('duration.hours', { ns: 'analytics', count: hours }))
  }
  if (minutes > 0 || parts.length === 0) {
    parts.push(i18n.t('duration.minutes', { ns: 'analytics', count: minutes }))
  }

  return parts.join(' ')
}

export function formatPercent(value: number): string {
  return `${value.toFixed(1)}%`
}

export function formatUtcDate(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return `${date.toISOString().slice(0, 10)}${i18n.t('utcSuffix', { ns: 'analytics' })}`
}

export function formatUtcDateTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return `${date.toISOString().replace('T', ' ').slice(0, 16)}${i18n.t('utcSuffix', { ns: 'analytics' })}`
}

export function todayIsoDate(): string {
  return new Date().toISOString().slice(0, 10)
}
