import type { OpportunityType } from '~/types'

const typesMapRu: Record<OpportunityType, string> = {
  EVENT: 'Мероприятие',
  VACANCY: 'Вакансия',
  INTERNSHIP: 'Стажировка',
  MENTORSHIP: 'Менторство',
}

const typesMapEn: Record<OpportunityType, string> = {
  EVENT: 'Event',
  VACANCY: 'Vacansy',
  INTERNSHIP: 'Internship',
  MENTORSHIP: 'Mentrorship',
}

export const formatOpporunityType = (type: OpportunityType, lang: 'ru' | 'en'): string => {
  if (lang === 'ru') return typesMapRu[type]
  return typesMapEn[type]
}
