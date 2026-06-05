// ==================== API Error Types ====================

export interface ApiError {
  timestamp: string
  status: number
  error?: string
  errors?: string[]
}
