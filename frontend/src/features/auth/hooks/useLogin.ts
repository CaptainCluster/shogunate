import { useMutation } from '@tanstack/react-query'
import { login } from '../../../api/authApi'
import { setAuthToken } from '../../../api/client'
import { authKeys } from '../authKeys'
import { queryClient } from '../../../lib/queryClient'

export function useLogin() {
  return useMutation({
    mutationFn: ({ username, password }: { username: string; password: string }) =>
      login(username, password),
    onSuccess: (data) => {
      queryClient.clear()
      setAuthToken(data.token)
      void queryClient.invalidateQueries({ queryKey: authKeys.me() })
    },
  })
}
