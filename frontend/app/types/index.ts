// ==================== Main Types Export ====================
// Re-export all types from a single entry point

// Auth
export type { UserRole } from './auth'
export type { LoginRequest, RegisterRequest, AuthResponse, CurrentUserResponse } from './auth'

// Tags
export type { TagCategory } from './tag'
export type { Tag, TagResponse, CreateTagRequest } from './tag'

// Employers
export type { EmployerStatus } from './employer'
export type {
  Employer,
  EmployerResponse,
  EmployerProfileResponse,
  RegisterEmployerRequest,
} from './employer'

// Opportunities
export type { OpportunityType, OpportunityFormat, OpportunityStatus } from './opportunity'
export type {
  OpportunityMiniCard,
  OpportunityMiniCardResponse,
  OpportunityCard,
  OpportunityCardResponse,
  CreateOpportunityRequest,
} from './opportunity'

// Applicants
export type { ApplicantVisibility } from './applicant'
export type {
  Applicant,
  ApplicantResponse,
  CreateApplicantRequest,
  UpdateApplicantRequest,
  UpdateApplicantVisibilityRequest,
} from './applicant'

// Contacts
export type { ApplicantContactStatus } from './contact'
export type {
  ApplicantContact,
  ApplicantContactResponse,
  ApplicantContactListItem,
  UpdateContactStatusRequest,
} from './contact'

// Favorites
export type {
  FavoriteOpportunity,
  FavoriteOpportunityResponse,
  AddFavoritesBulkRequest,
  AddFavoritesBulkResponse,
} from './favorite'

// Responses
export type { OpportunityResponseStatus } from './response'
export type {
  OpportunityResponse,
  OpportunityResponseListItem,
  OpportunityResponseApplicant,
  ApplicantResponsesLookup,
} from './response'

// Recommendations
export type { Recommendation, RecommendationResponse } from './recommendation'

// Curators
export type { Curator, CuratorResponse, CreateCuratorRequest } from './curator'

// Media
export type { MediaUploadResponse } from './media'

// Error
export type { ApiError } from './error'
