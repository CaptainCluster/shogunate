import { describe, expect, it } from 'vitest'
import { formatDuration, formatPercent, formatUtcDate } from './formatDuration'

describe('formatDuration', () => {
  it('returns 0m for zero or negative seconds', () => {
    expect(formatDuration(0)).toBe('0m')
    expect(formatDuration(-10)).toBe('0m')
  })

  it('formats minutes only', () => {
    expect(formatDuration(45 * 60)).toBe('45m')
  })

  it('formats hours and minutes', () => {
    expect(formatDuration(3 * 3600 + 30 * 60)).toBe('3h 30m')
  })

  it('formats days, hours, and minutes', () => {
    expect(formatDuration(2 * 86400 + 4 * 3600 + 15 * 60)).toBe('2d 4h 15m')
  })
})

describe('formatPercent', () => {
  it('formats to one decimal place', () => {
    expect(formatPercent(40)).toBe('40.0%')
    expect(formatPercent(33.333)).toBe('33.3%')
  })
})

describe('formatUtcDate', () => {
  it('formats ISO instant as UTC date', () => {
    expect(formatUtcDate('2024-06-15T14:30:00Z')).toBe('2024-06-15 UTC')
  })
})
