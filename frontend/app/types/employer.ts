// ==================== Employer Types ====================

export type EmployerStatus = 'pending' | 'verified'

export interface Employer {
  id: number
  name: string
  logoUrl?: string
  website?: string
  contacts?: string
}

export interface EmployerResponse {
  id: number
  companyName: string
  description?: string
  inn: string
  website?: string
  socials?: string
  logoUrl?: string
  verifiedOrgName?: string
  status: EmployerStatus
}

export interface EmployerProfileResponse {
  id: number
  userId?: number
  companyName: string
  description?: string
  inn: string
  website?: string
  socials?: string
  logoUrl?: string
  verifiedOrgName?: string
  status: EmployerStatus
}

export interface RegisterEmployerRequest {
  userId?: number
  companyName: string
  description?: string
  inn: string
  website?: string
  socials?: string
  logoUrl?: string
}
