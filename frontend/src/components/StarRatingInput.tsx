import { useCallback, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { getStarFill, isValidRating, ratingFromStarClick } from './starRatingUtils'
import './starRating.css'

interface StarRatingInputProps {
  value: number | null
  onChange: (rating: number) => void
  disabled?: boolean
  label?: string
}

export function StarRatingInput({
  value,
  onChange,
  disabled = false,
  label,
}: StarRatingInputProps) {
  const { t } = useTranslation('reviews')
  const resolvedLabel = label ?? t('rating')
  const [hoverRating, setHoverRating] = useState<number | null>(null)

  const displayRating = hoverRating ?? value

  const handleKeyDown = useCallback(
    (event: React.KeyboardEvent) => {
      if (disabled) {
        return
      }

      const current = isValidRating(value) ? value : 1

      if (event.key === 'ArrowRight' || event.key === 'ArrowUp') {
        event.preventDefault()
        onChange(Math.min(5, current + 0.5))
      } else if (event.key === 'ArrowLeft' || event.key === 'ArrowDown') {
        event.preventDefault()
        onChange(Math.max(1, current - 0.5))
      }
    },
    [disabled, onChange, value],
  )

  function clearHover() {
    setHoverRating(null)
  }

  return (
    <span
      className="star-rating star-rating--input"
      role="slider"
      tabIndex={disabled ? -1 : 0}
      aria-valuemin={1}
      aria-valuemax={5}
      aria-valuenow={isValidRating(value) ? value : undefined}
      aria-label={resolvedLabel}
      aria-disabled={disabled}
      onKeyDown={handleKeyDown}
      onMouseLeave={clearHover}
    >
      {[1, 2, 3, 4, 5].map((starIndex) => {
        const fill = isValidRating(displayRating) ? getStarFill(starIndex, displayRating) : 'empty'
        const leftRating = ratingFromStarClick(starIndex, 'left')
        const rightRating = ratingFromStarClick(starIndex, 'right')

        return (
          <span key={starIndex} className="star-rating__star-wrapper">
            <button
              type="button"
              className="star-rating__half star-rating__half--left"
              disabled={disabled}
              aria-label={
                leftRating % 1 === 0
                  ? t('setStars', { count: leftRating })
                  : t('setHalfStars', { count: Math.floor(leftRating) })
              }
              onMouseEnter={() => setHoverRating(leftRating)}
              onClick={() => onChange(leftRating)}
            />
            <button
              type="button"
              className="star-rating__half star-rating__half--right"
              disabled={disabled}
              aria-label={t('setStars', { count: rightRating })}
              onMouseEnter={() => setHoverRating(rightRating)}
              onClick={() => onChange(rightRating)}
            />
            <span className={`star-rating__star star-rating__star--${fill}`} aria-hidden>
              <span className="star-rating__star-bg">☆</span>
              <span className="star-rating__star-fill">★</span>
            </span>
          </span>
        )
      })}
    </span>
  )
}
