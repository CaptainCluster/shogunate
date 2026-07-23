import { QueryClient } from '@tanstack/react-query'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { setAuthToken } from '../../api/client'
import { showKeys } from '../library/showKeys'
import { authKeys } from './authKeys'
import { clearSession } from './clearSession'

const queryClientClear = vi.fn()
const queryClientCancelQueries = vi.fn()
const setAuthTokenMock = vi.fn()

vi.mock('../../api/client', () => ({
  setAuthToken: (...args: unknown[]) => setAuthTokenMock(...args),
}))

vi.mock('../../lib/queryClient', () => ({
  queryClient: {
    cancelQueries: (...args: unknown[]) => queryClientCancelQueries(...args),
    clear: () => queryClientClear(),
    setQueryData: vi.fn(),
    getQueryCache: vi.fn(),
  },
}))

describe('clearSession', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('clears auth token and wipes the query cache on logout', () => {
    clearSession()

    expect(setAuthTokenMock).toHaveBeenCalledWith(null)
    expect(queryClientCancelQueries).toHaveBeenCalled()
    expect(queryClientClear).toHaveBeenCalled()
  })
})

describe('session cache isolation', () => {
  it('removes seeded library data when clearSession runs', () => {
    const client = new QueryClient()
    client.setQueryData(showKeys.library(), [{ id: 'show-a', title: 'User A Show' }])

    expect(client.getQueryData(showKeys.library())).toHaveLength(1)

    client.clear()

    expect(client.getQueryData(showKeys.library())).toBeUndefined()
  })

  it('login flow clears stale cache before refetching current user', () => {
    const client = new QueryClient()
    client.setQueryData(showKeys.library(), [{ id: 'show-a', title: 'User A Show' }])
    client.setQueryData(authKeys.me(), { id: 'user-a', username: 'user_a' })

    client.clear()
    setAuthToken('token-for-user-b')
    void client.invalidateQueries({ queryKey: authKeys.me() })

    expect(client.getQueryData(showKeys.library())).toBeUndefined()
    expect(client.getQueryData(authKeys.me())).toBeUndefined()
  })
})
