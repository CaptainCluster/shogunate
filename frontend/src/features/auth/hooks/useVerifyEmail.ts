import { useMutation, useQueryClient } from '@tanstack/react-query'
import { verifyEmail } from '../../../api/authApi'
import { getAuthToken } from '../../../api/client'
import { authKeys } from '../authKeys'

export function useVerifyEmail() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (token: string) => verifyEmail(token),
    onSuccess: async () => {
      if (getAuthToken()) {
        await queryClient.invalidateQueries({ queryKey: authKeys.me() })
      }
    },
  })
}
