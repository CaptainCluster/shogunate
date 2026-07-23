import { setAuthToken } from '../../api/client'
import { queryClient } from '../../lib/queryClient'

export function clearSession() {
  setAuthToken(null)
  void queryClient.cancelQueries()
  queryClient.clear()
}
