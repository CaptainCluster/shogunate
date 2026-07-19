import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import * as authApi from '../api/authApi'
import { setAuthToken, type ApiError } from '../api/client'

interface AuthContextValue {
  user: authApi.UserResponse | null
  isLoading: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => void
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<authApi.UserResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const refreshUser = useCallback(async () => {
    try {
      const currentUser = await authApi.getCurrentUser()
      setUser(currentUser)
    } catch {
      setUser(null)
      setAuthToken(null)
    }
  }, [])

  useEffect(() => {
    void (async () => {
      await refreshUser()
      setIsLoading(false)
    })()
  }, [refreshUser])

  const login = useCallback(async (email: string, password: string) => {
    const response = await authApi.login(email, password)
    setAuthToken(response.token)
    await refreshUser()
  }, [refreshUser])

  const logout = useCallback(() => {
    setAuthToken(null)
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({ user, isLoading, login, logout, refreshUser }),
    [user, isLoading, login, logout, refreshUser],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}

export function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof Error && 'status' in error) {
    return (error as ApiError).message
  }
  if (error instanceof Error) {
    return error.message
  }
  return fallback
}
