export type OpportunityType = 'job' | 'internship' | 'event'

export interface OpportunityCard {
  id: number
  title: string
  description: string
  type: OpportunityType
  image: string
  tags: string[]
}
