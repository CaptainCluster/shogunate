import { useMutation, useQueryClient } from '@tanstack/react-query'
import { login } from '../../../api/authApi'
import { setAuthToken } from '../../../api/client'
import { authKeys } from '../authKeys'
import { fetchCurrentUser } from '../fetchCurrentUser'

export function useLogin() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ email, password }: { email: string; password: string }) =>
      login(email, password),
    onSuccess: async (response) => {
      setAuthToken(response.token)
      await queryClient.fetchQuery({
        queryKey: authKeys.me(),
        queryFn: fetchCurrentUser,
      })
    },
  })
}
