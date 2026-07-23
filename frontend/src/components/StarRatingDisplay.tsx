import { useTranslation } from 'react-i18next'
import { getStarFill } from './starRatingUtils'
import './starRating.css'

interface StarRatingDisplayProps {
  rating: number
  className?: string
  label?: string
}

export function StarRatingDisplay({ rating, className, label }: StarRatingDisplayProps) {
  const { t } = useTranslation('reviews')
  const stars = [1, 2, 3, 4, 5].map((index) => ({
    index,
    fill: getStarFill(index, rating),
  }))

  return (
    <span
      className={['star-rating', 'star-rating--display', className].filter(Boolean).join(' ')}
      role="img"
      aria-label={label ?? t('starRating')}
    >
      {stars.map(({ index, fill }) => (
        <span key={index} className={`star-rating__star star-rating__star--${fill}`} aria-hidden>
          <span className="star-rating__star-bg">☆</span>
          <span className="star-rating__star-fill">★</span>
        </span>
      ))}
    </span>
  )
}
