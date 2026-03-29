// ==================== Applicant Types ====================

import type { TagResponse } from './tag'

export type ApplicantVisibility = 'PUBLIC' | 'PRIVATE'

export interface Applicant {
  id: number
  userId: number
  name: string
  university?: string
  faculty?: string
  currentFieldOfStudy?: string
  desiredPosition?: string
  major?: string
  graduationYear?: number
  additionalEducationDetails?: string
  portfolioUrl?: string
  avatarUrl?: string
  resumeUrl?: string
  visibility: ApplicantVisibility
  skills: TagResponse[]
}

export interface ApplicantResponse {
  id: number
  userId: number
  name: string
  university?: string
  faculty?: string
  currentFieldOfStudy?: string
  desiredPosition?: string
  major?: string
  graduationYear?: number
  additionalEducationDetails?: string
  portfolioUrl?: string
  avatarUrl?: string
  resumeUrl?: string
  visibility: ApplicantVisibility
  skills: TagResponse[]
}

export interface CreateApplicantRequest {
  userId: number
  university?: string
  faculty?: string
  currentFieldOfStudy?: string
  desiredPosition?: string
  major?: string
  graduationYear?: number
  additionalEducationDetails?: string
  portfolioUrl?: string
  resumeUrl?: string
  skillTagIds?: number[]
}

export interface UpdateApplicantRequest {
  university?: string
  faculty?: string
  currentFieldOfStudy?: string
  desiredPosition?: string
  major?: string
  graduationYear?: number
  additionalEducationDetails?: string
  portfolioUrl?: string
  skillTagIds?: number[]
}

export interface UpdateApplicantVisibilityRequest {
  visibility: ApplicantVisibility
}
