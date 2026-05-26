import { STORAGE_TOKEN_KEY } from '@/constants'

export function getToken(): string {
  return localStorage.getItem(STORAGE_TOKEN_KEY) || ''
}

export function setToken(token: string): void {
  if (token) {
    localStorage.setItem(STORAGE_TOKEN_KEY, token)
  } else {
    localStorage.removeItem(STORAGE_TOKEN_KEY)
  }
}
