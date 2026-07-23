import { useCallback } from 'react'
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
    >
      {[1, 2, 3, 4, 5].map((starIndex) => {
        const fill = isValidRating(value) ? getStarFill(starIndex, value) : 'empty'

        return (
          <span key={starIndex} className="star-rating__star-wrapper">
            <button
              type="button"
              className="star-rating__half star-rating__half--left"
              disabled={disabled}
              aria-label={t('setStars', { count: starIndex })}
              onClick={() => onChange(ratingFromStarClick(starIndex, 'left'))}
            />
            <button
              type="button"
              className="star-rating__half star-rating__half--right"
              disabled={disabled}
              aria-label={t('setHalfStars', { count: starIndex })}
              onClick={() => onChange(ratingFromStarClick(starIndex, 'right'))}
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
