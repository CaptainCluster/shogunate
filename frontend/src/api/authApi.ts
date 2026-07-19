import { apiRequest } from './client'

export interface AuthResponse {
  token: string
  userId: string
  email: string
}

export interface UserResponse {
  id: string
  email: string
  emailVerified: boolean
}

export interface MessageResponse {
  message: string
}

export function register(email: string, password: string) {
  return apiRequest<MessageResponse>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export function login(email: string, password: string) {
  return apiRequest<AuthResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export function verifyEmail(token: string) {
  return apiRequest<MessageResponse>('/api/auth/verify-email', {
    method: 'POST',
    body: JSON.stringify({ token }),
  })
}

export function resendVerification(email: string) {
  return apiRequest<MessageResponse>('/api/auth/resend-verification', {
    method: 'POST',
    body: JSON.stringify({ email }),
  })
}

export function forgotPassword(email: string) {
  return apiRequest<MessageResponse>('/api/auth/forgot-password', {
    method: 'POST',
    body: JSON.stringify({ email }),
  })
}

export function resetPassword(token: string, password: string) {
  return apiRequest<MessageResponse>('/api/auth/reset-password', {
    method: 'POST',
    body: JSON.stringify({ token, password }),
  })
}

export function getCurrentUser() {
  return apiRequest<UserResponse>('/api/me')
}
