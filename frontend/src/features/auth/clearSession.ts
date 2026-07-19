import { setAuthToken } from '../../api/client'
import { queryClient } from '../../lib/queryClient'
import { authKeys } from './authKeys'

export function clearSession() {
  setAuthToken(null)
  void queryClient.cancelQueries({ queryKey: authKeys.all })
  queryClient.removeQueries({ queryKey: authKeys.all })
}
