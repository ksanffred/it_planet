export type OpportunityType = 'job' | 'internship' | 'event'

export interface OpportunityCard {
  title: string
  description: string
  type: OpportunityType
  image: string
  tags: string[]
}
