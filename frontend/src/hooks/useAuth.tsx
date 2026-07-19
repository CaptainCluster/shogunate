import { useLogin } from '../features/auth/hooks/useLogin'
import { useLogout } from '../features/auth/hooks/useLogout'
import { useCurrentUser } from '../features/auth/hooks/useCurrentUser'

export function useAuth() {
  const { data: user, isLoading } = useCurrentUser()
  const loginMutation = useLogin()
  const logout = useLogout()

  return {
    user: user ?? null,
    isLoading,
    login: async (username: string, password: string) => {
      await loginMutation.mutateAsync({ username, password })
    },
    logout,
  }
}
