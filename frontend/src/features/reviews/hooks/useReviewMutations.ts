import { useMutation, useQueryClient } from '@tanstack/react-query'
import type {
  CreateReviewPayload,
  Review,
  ReviewTargetType,
  UpdateReviewPayload,
} from '../../../api/reviewApi'
import * as reviewApi from '../../../api/reviewApi'
import { reviewKeys } from '../reviewKeys'

function invalidateReview(queryClient: ReturnType<typeof useQueryClient>, review: Review) {
  return queryClient.invalidateQueries({
    queryKey: reviewKeys.target(review.targetType, review.targetId),
  })
}

export function useReviewMutations(targetType: ReviewTargetType, targetId: string) {
  const queryClient = useQueryClient()

  const createReview = useMutation({
    mutationFn: (payload: Omit<CreateReviewPayload, 'targetType' | 'targetId'>) =>
      reviewApi.createReview({ targetType, targetId, ...payload }),
    onSuccess: (review) => invalidateReview(queryClient, review),
  })

  const updateReview = useMutation({
    mutationFn: ({ id, ...payload }: UpdateReviewPayload & { id: string }) =>
      reviewApi.updateReview(id, payload),
    onSuccess: (review) => invalidateReview(queryClient, review),
  })

  const deleteReview = useMutation({
    mutationFn: (id: string) => reviewApi.deleteReview(id),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: reviewKeys.target(targetType, targetId) }),
  })

  const error = createReview.error ?? updateReview.error ?? deleteReview.error
  const isPending = createReview.isPending || updateReview.isPending || deleteReview.isPending

  return {
    createReview,
    updateReview,
    deleteReview,
    error,
    isPending,
  }
}
