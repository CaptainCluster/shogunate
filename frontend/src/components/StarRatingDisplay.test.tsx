import { describe, expect, it } from 'vitest'
import { renderWithI18n } from '../test/renderWithI18n'
import { StarRatingDisplay } from './StarRatingDisplay'

describe('StarRatingDisplay', () => {
  it('renders five filled stars for a perfect rating', () => {
    const { container } = renderWithI18n(<StarRatingDisplay rating={5} />)

    expect(container.querySelectorAll('.star-rating__star--full')).toHaveLength(5)
    expect(container.querySelectorAll('.star-rating__star--half')).toHaveLength(0)
    expect(container.textContent).not.toMatch(/\d/)
  })

  it('renders half and empty stars for a mid rating', () => {
    const { container } = renderWithI18n(<StarRatingDisplay rating={3.5} />)

    expect(container.querySelectorAll('.star-rating__star--full')).toHaveLength(3)
    expect(container.querySelectorAll('.star-rating__star--half')).toHaveLength(1)
    expect(container.querySelectorAll('.star-rating__star--empty')).toHaveLength(1)
    expect(container.textContent).not.toMatch(/3\.5/)
  })
})
