// ==================== Opportunity Types ====================

import type { EmployerResponse } from './employer'
import type { TagResponse } from './tag'

export type OpportunityType = 'VACANCY' | 'INTERNSHIP' | 'MENTORSHIP' | 'EVENT'

export type OpportunityFormat = 'OFFICE' | 'HYBRID' | 'REMOTE'

export type OpportunityStatus = 'ACTIVE' | 'CLOSED' | 'PLANNED'

/**
 * Мини-карточка возможности для главной страницы
 * @property id - Уникальный идентификатор
 * @property media - URL первого медиа (изображение)
 * @property title - Заголовок возможности
 * @property description - Краткое описание
 * @property employerName - Название компании/работодателя
 * @property format - Формат работы (OFFICE/HYBRID/REMOTE)
 * @property type - Тип возможности (VACANSY/INTERNSHIP/MENTORSHIP/EVENT)
 * @property tags - Список тегов (технологии, уровень)
 */
export interface OpportunityMiniCard {
  id: number
  media: string
  title: string
  description: string
  employerName: string
  format: OpportunityFormat
  type: OpportunityType
  city: string
  address: string
  lat: number
  lng: number
  tags: string[]
}

export interface OpportunityMiniCardResponse {
  id: number
  media: string
  title: string
  description: string
  employerName: string
  format: OpportunityFormat
  tags: string[]
}

export interface OpportunityCard {
  id: number
  title: string
  description: string
  media: string[]
  employer: EmployerResponse
  type: OpportunityType
  format: OpportunityFormat
  address?: string
  city?: string
  salaryFrom?: number
  salaryTo?: number
  publishedAt?: string
  expiresAt?: string
  status: OpportunityStatus
  tags: TagResponse[]
}

export interface OpportunityCardResponse {
  id: number
  title: string
  description: string
  media: string[]
  employer: EmployerResponse
  type: OpportunityType
  format: OpportunityFormat
  address?: string
  city?: string
  lat?: number
  lng?: number
  salaryFrom?: number
  salaryTo?: number
  publishedAt?: string
  expiresAt?: string
  status: OpportunityStatus
  tags: TagResponse[]
}

export interface CreateOpportunityRequest {
  employerId: number
  title: string
  description?: string
  type: OpportunityType
  format: OpportunityFormat
  address?: string
  city?: string
  lat?: number
  lng?: number
  salaryFrom?: number
  salaryTo?: number
  publishedAt?: string
  expiresAt?: string
  status: OpportunityStatus
  media?: string[]
  tagIds?: number[]
}
