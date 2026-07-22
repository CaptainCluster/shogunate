import { apiRequest } from './client'

export type ReviewTargetType = 'EPISODE' | 'SEASON' | 'SHOW'

export interface Review {
  id: string
  targetType: ReviewTargetType
  targetId: string
  rating: number
  body: string | null
  createdAt: string
  updatedAt: string | null
}

export interface CreateReviewPayload {
  targetType: ReviewTargetType
  targetId: string
  rating: number
  body?: string | null
}

export interface UpdateReviewPayload {
  rating: number
  body?: string | null
}

export function getReview(targetType: ReviewTargetType, targetId: string) {
  const params = new URLSearchParams({ targetType, targetId })
  return apiRequest<Review>(`/api/reviews?${params}`)
}

export function createReview(payload: CreateReviewPayload) {
  return apiRequest<Review>('/api/reviews', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateReview(id: string, payload: UpdateReviewPayload) {
  return apiRequest<Review>(`/api/reviews/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteReview(id: string) {
  return apiRequest<void>(`/api/reviews/${id}`, {
    method: 'DELETE',
  })
}
