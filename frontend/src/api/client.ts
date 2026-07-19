const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

const AUTH_TOKEN_KEY = 'auth_token'
const AUTH_TOKEN_CHANGED = 'auth_token_changed'

export function getAuthToken(): string | null {
  return localStorage.getItem(AUTH_TOKEN_KEY)
}

export function getAuthTokenSnapshot(): string | null {
  return getAuthToken()
}

export function subscribeAuthToken(onChange: () => void) {
  const handleTokenChange = (event: Event) => {
    if (event.type === AUTH_TOKEN_CHANGED) {
      onChange()
      return
    }
    const storageEvent = event as StorageEvent
    if (storageEvent.key === AUTH_TOKEN_KEY) {
      onChange()
    }
  }

  window.addEventListener(AUTH_TOKEN_CHANGED, handleTokenChange)
  window.addEventListener('storage', handleTokenChange)

  return () => {
    window.removeEventListener(AUTH_TOKEN_CHANGED, handleTokenChange)
    window.removeEventListener('storage', handleTokenChange)
  }
}

export function setAuthToken(token: string | null) {
  if (token) {
    localStorage.setItem(AUTH_TOKEN_KEY, token)
  } else {
    localStorage.removeItem(AUTH_TOKEN_KEY)
  }
  window.dispatchEvent(new Event(AUTH_TOKEN_CHANGED))
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const headers = new Headers(options.headers)
  if (!headers.has('Content-Type') && options.body) {
    headers.set('Content-Type', 'application/json')
  }

  const token = getAuthToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  })

  if (!response.ok) {
    let message = response.statusText
    try {
      const body = (await response.json()) as { message?: string }
      if (body.message) {
        message = body.message
      }
    } catch {
      // ignore parse errors
    }
    throw new ApiError(message, response.status)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}
