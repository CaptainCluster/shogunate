export function formatDuration(seconds: number): string {
  if (seconds <= 0) {
    return '0m'
  }

  const days = Math.floor(seconds / 86_400)
  const hours = Math.floor((seconds % 86_400) / 3_600)
  const minutes = Math.floor((seconds % 3_600) / 60)

  const parts: string[] = []
  if (days > 0) {
    parts.push(`${days}d`)
  }
  if (hours > 0) {
    parts.push(`${hours}h`)
  }
  if (minutes > 0 || parts.length === 0) {
    parts.push(`${minutes}m`)
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

  return `${date.toISOString().slice(0, 10)} UTC`
}

export function formatUtcDateTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return `${date.toISOString().replace('T', ' ').slice(0, 16)} UTC`
}

export function todayIsoDate(): string {
  return new Date().toISOString().slice(0, 10)
}
