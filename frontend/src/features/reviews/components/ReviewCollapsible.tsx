import type { ReviewTargetType } from '../../../api/reviewApi'
import { StarRatingDisplay } from '../../../components/StarRatingDisplay'
import { useReview } from '../hooks/useReview'
import { ReviewEditor } from './ReviewEditor'
import '../reviews.css'

interface ReviewCollapsibleProps {
  targetType: ReviewTargetType
  targetId: string
  label?: string
}

export function ReviewCollapsible({ targetType, targetId, label }: ReviewCollapsibleProps) {
  const review = useReview(targetType, targetId)

  return (
    <details className="review-collapsible">
      <summary className="review-collapsible__summary">
        <span className="review-collapsible__label">Review</span>
        {review.data && <StarRatingDisplay rating={review.data.rating} label="Your rating" />}
      </summary>
      <div className="review-collapsible__content">
        <ReviewEditor targetType={targetType} targetId={targetId} label={label} />
      </div>
    </details>
  )
}
