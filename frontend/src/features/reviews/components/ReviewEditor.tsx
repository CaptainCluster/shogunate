import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { Review, ReviewTargetType } from '../../../api/reviewApi'
import { isValidRating } from '../../../components/starRatingUtils'
import { StarRatingInput } from '../../../components/StarRatingInput'
import { getErrorMessage } from '../../../lib/getErrorMessage'
import { useReview } from '../hooks/useReview'
import { useReviewMutations } from '../hooks/useReviewMutations'
import '../reviews.css'

export interface ReviewEditorProps {
  targetType: ReviewTargetType
  targetId: string
  label?: string
  compact?: boolean
  collapseExistingReview?: boolean
}

interface ReviewEditorFormProps {
  targetType: ReviewTargetType
  targetId: string
  label?: string
  review: Review | null
  compact?: boolean
  collapseExistingReview?: boolean
}

function ReviewEditorForm({
  targetType,
  targetId,
  label,
  review,
  compact = false,
  collapseExistingReview = false,
}: ReviewEditorFormProps) {
  const { t } = useTranslation('reviews')
  const mutations = useReviewMutations(targetType, targetId)
  const [rating, setRating] = useState<number | null>(review?.rating ?? null)
  const [body, setBody] = useState(review?.body ?? '')
  const [detailsExpanded, setDetailsExpanded] = useState(false)

  const hasSavedReview = review !== null
  const canCollapseExisting = collapseExistingReview && hasSavedReview
  const showReviewDetails = canCollapseExisting
    ? detailsExpanded && isValidRating(rating)
    : isValidRating(rating)

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

  return (
    <div className={`review-editor${compact ? ' review-editor--compact' : ''}`}>
      <div className="review-editor__rating-row">
        <StarRatingInput
          value={rating}
          onChange={setRating}
          disabled={mutations.isPending}
          label={label ?? t('rating')}
        />
        {canCollapseExisting && (
          <button
            type="button"
            className={`review-editor__expand${detailsExpanded ? ' review-editor__expand--open' : ''}`}
            aria-expanded={detailsExpanded}
            aria-label={detailsExpanded ? t('hideDetails') : t('showDetails')}
            disabled={mutations.isPending}
            onClick={() => setDetailsExpanded((open) => !open)}
          >
            ▼
          </button>
        )}
      </div>
      {showReviewDetails && (
        <>
          <label className="review-editor__label" htmlFor={fieldId}>
            {compact ? t('review') : t('reviewText')}
          </label>
          <textarea
            id={fieldId}
            className="review-editor__body"
            value={body}
            disabled={mutations.isPending}
            rows={compact ? 2 : 3}
            placeholder={compact ? t('placeholder') : undefined}
            onChange={(event) => setBody(event.target.value)}
          />
          <div className="review-editor__actions">
            <button
              type="button"
              disabled={mutations.isPending || !isValidRating(rating)}
              onClick={handleSave}
            >
              {t('saveReview')}
            </button>
            {review && (
              <button
                type="button"
                className="review-editor__delete"
                disabled={mutations.isPending}
                onClick={handleDelete}
              >
                {t('deleteReview')}
              </button>
            )}
          </div>
        </>
      )}
      {mutations.error && (
        <p className="library-error review-error">
          {getErrorMessage(mutations.error, t('updateFailed'))}
        </p>
      )}
    </div>
  )
}

export function ReviewEditor({
  targetType,
  targetId,
  label,
  compact = false,
  collapseExistingReview = false,
}: ReviewEditorProps) {
  const { t } = useTranslation('reviews')
  const review = useReview(targetType, targetId)

  if (review.isLoading) {
    return <p className="review-loading">{t('loading')}</p>
  }

  if (review.error) {
    return (
      <p className="library-error review-error">
        {getErrorMessage(review.error, t('loadFailed'))}
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
      collapseExistingReview={collapseExistingReview}
    />
  )
}
