import { getCurrentUser } from '../../api/authApi'
import { ApiError } from '../../api/client'
import { clearSession } from './clearSession'

export async function fetchCurrentUser() {
  try {
    return await getCurrentUser()
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      clearSession()
    }
    throw error
  }
}
