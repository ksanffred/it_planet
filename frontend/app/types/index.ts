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

// Media
export type { MediaUploadResponse } from './media'

// Error
export type { ApiError } from './error'
