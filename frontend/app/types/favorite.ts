// ==================== Applicant Favorite Types ====================

import type { OpportunityStatus, OpportunityType } from './opportunity'

export interface FavoriteOpportunity {
  id: number
  title: string
  company_name: string
  status: OpportunityStatus
  type: OpportunityType
}

export interface FavoriteOpportunityResponse {
  id: number
  title: string
  company_name: string
  status: OpportunityStatus
  type: OpportunityType
}

export interface AddFavoritesBulkRequest {
  opportunityIds: number[]
}

export interface AddFavoritesBulkResponse {
  applicantId: number
  opportunityIds: number[]
}
