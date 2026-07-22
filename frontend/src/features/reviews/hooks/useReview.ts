import { useQuery } from '@tanstack/react-query'
import type { ReviewTargetType } from '../../../api/reviewApi'
import * as reviewApi from '../../../api/reviewApi'
import { ApiError } from '../../../api/client'
import { reviewKeys } from '../reviewKeys'

export function useReview(targetType: ReviewTargetType, targetId: string) {
  return useQuery({
    queryKey: reviewKeys.target(targetType, targetId),
    queryFn: async () => {
      try {
        return await reviewApi.getReview(targetType, targetId)
      } catch (error) {
        if (error instanceof ApiError && error.status === 404) {
          return null
        }
        throw error
      }
    },
    enabled: !!targetId,
  })
}
