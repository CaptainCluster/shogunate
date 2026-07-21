export type WatchTargetType = 'EPISODE' | 'SEASON' | 'SHOW'

export const watchKeys = {
  all: ['watch'] as const,
  target: (targetType: WatchTargetType, targetId: string) =>
    [...watchKeys.all, targetType, targetId] as const,
}
