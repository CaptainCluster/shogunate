export type StarFill = 'empty' | 'half' | 'full'

export function getStarFill(starIndex: number, rating: number): StarFill {
  if (rating >= starIndex) {
    return 'full'
  }
  if (rating >= starIndex - 0.5) {
    return 'half'
  }
  return 'empty'
}

export function isValidRating(rating: number | null): rating is number {
  if (rating === null || rating < 1 || rating > 5) {
    return false
  }
  return Number.isInteger(rating * 2)
}

export function ratingFromStarClick(starIndex: number, half: 'left' | 'right'): number {
  if (half === 'right') {
    return starIndex
  }
  return Math.max(1, starIndex - 0.5)
}
