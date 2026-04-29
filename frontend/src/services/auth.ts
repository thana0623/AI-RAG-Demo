import request from './request'
import type { ApiResult } from '@/types/api'
import type {
  LoginPayload,
  RegisterPayload,
  ResetPasswordPayload,
  UserProfile
} from '@/types/user'

export function sendCode(email: string, type: 'REGISTER' | 'RESET') {
  return request.post<unknown, ApiResult<void>>('/auth/send-code', { email, type })
}

export function register(payload: RegisterPayload) {
  return request.post<unknown, ApiResult<void>>('/auth/register', payload)
}

export function login(payload: LoginPayload) {
  return request.post<unknown, ApiResult<string>>('/auth/login', payload)
}

export function resetPassword(payload: ResetPasswordPayload) {
  return request.post<unknown, ApiResult<void>>('/auth/reset-password', payload)
}

export function getCurrentUser() {
  return request.get<unknown, ApiResult<UserProfile>>('/auth/current')
}
