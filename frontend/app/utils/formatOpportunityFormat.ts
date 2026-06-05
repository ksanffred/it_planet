import type { OpportunityFormat } from '~/types'

const formatsMapRu: Record<OpportunityFormat, string> = {
  OFFICE: 'Офис',
  HYBRID: 'Гибрид',
  REMOTE: 'Удаленно',
}

const formatsMapEn: Record<OpportunityFormat, string> = {
  OFFICE: 'Office',
  HYBRID: 'Hybrid',
  REMOTE: 'Remote',
}

export const formatOpporunityFormat = (format: OpportunityFormat, lang: 'ru' | 'en'): string => {
  if (lang === 'ru') return formatsMapRu[format]
  return formatsMapEn[format]
}
