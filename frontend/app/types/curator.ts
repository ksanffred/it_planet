// ==================== Curator Types ====================

export interface Curator {
  id: number
  userId: number
}

export interface CuratorResponse {
  id: number
  userId: number
}

export interface CreateCuratorRequest {
  userId: number
}
