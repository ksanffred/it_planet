const internshipOpportunity = ref(false)
const eventOpportunity = ref(false)
const vacansyOpportunity = ref(false)

export const useOpportunityFilters = () => {
  const activeTypes = computed(() => {
    const types: string[] = []
    if (internshipOpportunity.value) types.push('INTERNSHIP')
    if (eventOpportunity.value) types.push('EVENT')
    if (vacansyOpportunity.value) types.push('VACANCY')
    return types
  })

  const clearTypes = () => {
    internshipOpportunity.value = false
    eventOpportunity.value = false
    vacansyOpportunity.value = false
  }

  return {
    internshipOpportunity,
    eventOpportunity,
    vacansyOpportunity,
    activeTypes,
    clearTypes,
  }
}
