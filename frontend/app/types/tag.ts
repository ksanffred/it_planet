// ==================== Tag Types ====================

export type TagCategory = 'TECHNOLOGY' | 'LEVEL' | 'EMPLOYMENT_TYPE'

export interface Tag {
  id: number
  name: string
  category: TagCategory
}

export interface TagResponse {
  id: number
  name: string
  category: TagCategory
}

export interface CreateTagRequest {
  name: string
  category: TagCategory
}
