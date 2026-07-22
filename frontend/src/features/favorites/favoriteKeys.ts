export const favoriteKeys = {
  all: ['favorites'] as const,
  list: () => [...favoriteKeys.all, 'list'] as const,
  suggestions: () => [...favoriteKeys.all, 'suggestions'] as const,
  status: (showId: string) => [...favoriteKeys.all, 'status', showId] as const,
}
