export interface UserProfile {
  id?: number
  username: string
  email: string
}

export interface LoginPayload {
  identifier: string
  password: string
}

export interface RegisterPayload {
  email: string
  username: string
  password: string
  code: string
}

export interface ResetPasswordPayload {
  email: string
  newPassword: string
  code: string
}
