import { useState } from 'react'
import type { Review, ReviewTargetType } from '../../../api/reviewApi'
import { isValidRating } from '../../../components/starRatingUtils'
import { StarRatingInput } from '../../../components/StarRatingInput'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { useReview } from '../hooks/useReview'
import { useReviewMutations } from '../hooks/useReviewMutations'
import '../reviews.css'

interface ReviewEditorProps {
  targetType: ReviewTargetType
  targetId: string
  label?: string
  compact?: boolean
}

interface ReviewEditorFormProps {
  targetType: ReviewTargetType
  targetId: string
  label?: string
  review: Review | null
  compact?: boolean
}

function ReviewEditorForm({
  targetType,
  targetId,
  label,
  review,
  compact = false,
}: ReviewEditorFormProps) {
  const mutations = useReviewMutations(targetType, targetId)
  const [rating, setRating] = useState<number | null>(review?.rating ?? null)
  const [body, setBody] = useState(review?.body ?? '')

  function handleSave() {
    if (!isValidRating(rating)) {
      return
    }

    const trimmedBody = body.trim()
    const payload = {
      rating,
      body: trimmedBody.length > 0 ? trimmedBody : null,
    }

    if (review) {
      mutations.updateReview.mutate({ id: review.id, ...payload })
      return
    }

    mutations.createReview.mutate(payload)
  }

  function handleDelete() {
    if (!review) {
      return
    }
    mutations.deleteReview.mutate(review.id)
  }

  const fieldId = `review-body-${targetType}-${targetId}`
  const showReviewDetails = isValidRating(rating)

  return (
    <div className={`review-editor${compact ? ' review-editor--compact' : ''}`}>
      <div className="review-editor__rating">
        <StarRatingInput
          value={rating}
          onChange={setRating}
          disabled={mutations.isPending}
          label={label ?? 'Rating'}
        />
      </div>
      {showReviewDetails && (
        <>
          <label className="review-editor__label" htmlFor={fieldId}>
            {compact ? 'Review' : 'Review text'}
          </label>
          <textarea
            id={fieldId}
            className="review-editor__body"
            value={body}
            disabled={mutations.isPending}
            rows={compact ? 2 : 3}
            placeholder={compact ? 'Write a review…' : undefined}
            onChange={(event) => setBody(event.target.value)}
          />
          <div className="review-editor__actions">
            <button
              type="button"
              disabled={mutations.isPending || !isValidRating(rating)}
              onClick={handleSave}
            >
              Save review
            </button>
            {review && (
              <button
                type="button"
                className="review-editor__delete"
                disabled={mutations.isPending}
                onClick={handleDelete}
              >
                Delete review
              </button>
            )}
          </div>
        </>
      )}
      {mutations.error && (
        <p className="library-error review-error">
          {getErrorMessage(mutations.error, 'Review update failed')}
        </p>
      )}
    </div>
  )
}

export function ReviewEditor({ targetType, targetId, label, compact = false }: ReviewEditorProps) {
  const review = useReview(targetType, targetId)

  if (review.isLoading) {
    return <p className="review-loading">Loading review…</p>
  }

  if (review.error) {
    return (
      <p className="library-error review-error">
        {getErrorMessage(review.error, 'Failed to load review')}
      </p>
    )
  }

  const formKey = review.data
    ? `${review.data.id}-${review.data.updatedAt ?? 'created'}`
    : 'empty'

  return (
    <ReviewEditorForm
      key={formKey}
      targetType={targetType}
      targetId={targetId}
      label={label}
      review={review.data ?? null}
      compact={compact}
    />
  )
}
