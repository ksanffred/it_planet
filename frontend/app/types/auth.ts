// ==================== Auth Types ====================

export type UserRole = 'USER' | 'APPLICANT' | 'EMPLOYER' | 'CURATOR'

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  displayName: string
  password: string
  role: UserRole
}

export interface AuthResponse {
  token: string
  userId: number
  email: string
  displayName: string
  role: UserRole
}

export interface CurrentUserResponse {
  id: number
  email: string
  displayName: string
  role: UserRole
}
