import type { ReviewTargetType } from '../../api/reviewApi'

export const reviewKeys = {
  all: ['reviews'] as const,
  target: (targetType: ReviewTargetType, targetId: string) =>
    [...reviewKeys.all, targetType, targetId] as const,
}
