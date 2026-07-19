import { apiRequest } from './client'

export interface AuthResponse {
  token: string
  userId: string
  username: string
}

export interface UserResponse {
  id: string
  username: string
}

export interface MessageResponse {
  message: string
}

export function register(username: string, password: string) {
  return apiRequest<MessageResponse>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

export function login(username: string, password: string) {
  return apiRequest<AuthResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

export function getCurrentUser() {
  return apiRequest<UserResponse>('/api/me')
}
