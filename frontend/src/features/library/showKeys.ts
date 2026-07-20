export const showKeys = {
  all: ['shows'] as const,
  library: () => [...showKeys.all, 'library'] as const,
  search: (query: string) => [...showKeys.all, 'search', query] as const,
  detail: (id: string) => [...showKeys.all, 'detail', id] as const,
}
