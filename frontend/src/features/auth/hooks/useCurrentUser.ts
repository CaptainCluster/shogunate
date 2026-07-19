import { useSyncExternalStore } from 'react'
import { useQuery } from '@tanstack/react-query'
import { ApiError, getAuthTokenSnapshot, subscribeAuthToken } from '../../../api/client'
import { authKeys } from '../authKeys'
import { fetchCurrentUser } from '../fetchCurrentUser'

export function useCurrentUser() {
  const token = useSyncExternalStore(subscribeAuthToken, getAuthTokenSnapshot, () => null)
  const hasToken = !!token

  const query = useQuery({
    queryKey: authKeys.me(),
    queryFn: fetchCurrentUser,
    enabled: hasToken,
    refetchOnWindowFocus: true,
    staleTime: 0,
    retry: (failureCount, error) => {
      if (error instanceof ApiError && error.status === 401) {
        return false
      }
      return failureCount < 1
    },
  })

  const isLoading = hasToken ? query.isPending && query.isFetching : false
  const data = hasToken ? query.data : undefined

  return {
    ...query,
    data,
    isLoading,
  }
}
