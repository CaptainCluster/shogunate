import { useCallback } from 'react'
import { clearSession } from '../clearSession'

export function useLogout() {
  return useCallback(() => {
    clearSession()
  }, [])
}
