// ==================== Applicant Contact Types ====================

export type ApplicantContactStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED'

export interface ApplicantContact {
  id: number
  requesterApplicantId: number
  recipientApplicantId: number
  status: ApplicantContactStatus
  createdAt: string
  updatedAt: string
}

export interface ApplicantContactResponse {
  id: number
  requesterApplicantId: number
  recipientApplicantId: number
  status: ApplicantContactStatus
  createdAt: string
  updatedAt: string
}

export interface ApplicantContactListItem {
  photo: string
  name: string
  desired_profession: string
  status: 'accepted' | 'sent' | 'received'
}

export interface UpdateContactStatusRequest {
  status: ApplicantContactStatus
}
