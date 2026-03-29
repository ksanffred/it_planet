// ==================== Recommendation Types ====================

export interface Recommendation {
  id: number
  recommenderApplicantId: number
  recommendedApplicantId: number
  opportunityId: number
  createdAt: string
}

export interface RecommendationResponse {
  id: number
  recommenderApplicantId: number
  recommendedApplicantId: number
  opportunityId: number
  createdAt: string
}
