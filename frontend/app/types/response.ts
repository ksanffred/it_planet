// ==================== Opportunity Response Types ====================

import type { OpportunityStatus, OpportunityType } from './opportunity'
import type { TagResponse } from './tag'

export type OpportunityResponseStatus = 'NOT_REVIEWED' | 'REVIEWED' | 'ACCEPTED' | 'REJECTED'

export interface OpportunityResponse {
  id: number
  opportunityId: number
  applicantId: number
  status: OpportunityResponseStatus
  createdAt: string
  updatedAt: string
}

export interface OpportunityResponseListItem {
  title: string
  company_name: string
  response_status: OpportunityResponseStatus
  opportunity_type: OpportunityType
  opportunity_status: OpportunityStatus
}

export interface OpportunityResponseApplicant {
  applicant_id: number
  applicant_name: string
  university: string
  desired_position: string
  recommendation: number
  matching_tags: TagResponse[]
}

export interface ApplicantResponsesLookup {
  title: string
  company_name: string
  response_status: OpportunityResponseStatus
  opportunity_type: OpportunityType
  opportunity_status: OpportunityStatus
}
