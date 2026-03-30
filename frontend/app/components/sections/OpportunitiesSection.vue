<script setup lang="ts">
import type {
  AddFavoritesBulkResponse,
  FavoriteOpportunityResponse,
  OpportunityMiniCard,
} from '@/types'

const config = useRuntimeConfig()
const route = useRoute()
const tokenCookie = useCookie<string | null>('auth_token')

const searchQuery = computed(() => {
  const value = route.query.search
  return typeof value === 'string' ? value.trim() : ''
})

const {
  data: miniCards,
  error,
  pending,
} = await useFetch<OpportunityMiniCard[]>('/opportunities/mini-cards', {
  baseURL: config.public.apiBase,
  method: 'GET',
  lazy: true,
  query: computed(() => ({
    search: searchQuery.value || undefined,
  })),
  watch: [searchQuery],
})

const PAGE_SIZE = 9
const visibleCardsCount = ref(PAGE_SIZE)

const visibleCards = computed(() => (miniCards.value ?? []).slice(0, visibleCardsCount.value))
const hasMoreCards = computed(() => visibleCardsCount.value < (miniCards.value?.length ?? 0))
const totalCardsCount = computed(() => miniCards.value?.length ?? 0)
const showEmptyState = computed(() => !pending.value && totalCardsCount.value === 0)
const showEndState = computed(
  () => !pending.value && totalCardsCount.value > 0 && !hasMoreCards.value,
)

const favoriteIds = ref<number[]>([])
const favoriteLoadingIds = ref<number[]>([])
const favoriteSignatures = ref<string[]>([])

type FavoriteCardLookup = {
  id?: number
  opportunityId?: number
  title?: string
  company_name?: string
  companyName?: string
  type?: string
}

const normalizeValue = (value: string | undefined) => (value ?? '').trim().toLowerCase()

const buildSignature = (title: string, company: string, type: string) =>
  `${normalizeValue(title)}|${normalizeValue(company)}|${normalizeValue(type)}`

const applyFavoriteSignatures = () => {
  if (favoriteSignatures.value.length === 0 || !miniCards.value?.length) {
    return
  }

  const signatureSet = new Set(favoriteSignatures.value)
  favoriteIds.value = miniCards.value
    .filter((card) => signatureSet.has(buildSignature(card.title, card.employerName, card.type)))
    .map((card) => card.id)
}

const extractFavoriteIds = (payload: unknown): number[] => {
  if (Array.isArray(payload)) {
    if (payload.every((item) => typeof item === 'number')) {
      return payload as number[]
    }

    return payload
      .map((item) => {
        if (!item || typeof item !== 'object') return null
        const typedItem = item as Partial<FavoriteOpportunityResponse> & {
          opportunityId?: number
        }
        return typedItem.id ?? typedItem.opportunityId ?? null
      })
      .filter((id): id is number => typeof id === 'number')
  }

  if (payload && typeof payload === 'object') {
    const typedPayload = payload as Partial<AddFavoritesBulkResponse>
    if (Array.isArray(typedPayload.opportunityIds)) {
      return typedPayload.opportunityIds
    }
  }

  return []
}

const extractFavoriteSignatures = (payload: unknown): string[] => {
  if (!Array.isArray(payload)) {
    return []
  }

  return payload
    .map((item) => {
      if (!item || typeof item !== 'object') return null
      const typedItem = item as FavoriteCardLookup
      if (!typedItem.title || !typedItem.type) return null
      const company = typedItem.company_name ?? typedItem.companyName ?? ''
      return buildSignature(typedItem.title, company, typedItem.type)
    })
    .filter((signature): signature is string => Boolean(signature))
}

const loadFavorites = async () => {
  if (!tokenCookie.value) {
    favoriteIds.value = []
    return
  }

  try {
    const response = await $fetch<FavoriteOpportunityResponse[] | AddFavoritesBulkResponse>(
      '/applicants/me/favorites/opportunities',
      {
        baseURL: config.public.apiBase,
        method: 'GET',
        headers: {
          Authorization: `Bearer ${tokenCookie.value}`,
        },
      },
    )

    const ids = extractFavoriteIds(response)
    if (ids.length > 0) {
      favoriteIds.value = ids
      favoriteSignatures.value = []
      return
    }

    favoriteSignatures.value = extractFavoriteSignatures(response)
    applyFavoriteSignatures()
  } catch (error) {
    favoriteIds.value = []
    favoriteSignatures.value = []
    console.error('Failed to load favorites', error)
  }
}

const isFavorite = (id: number) => favoriteIds.value.includes(id)
const isFavoriteLoading = (id: number) => favoriteLoadingIds.value.includes(id)

const toggleFavorite = async (opportunityId: number) => {
  if (!tokenCookie.value) {
    navigateTo('/auth/login')
    return
  }

  if (isFavoriteLoading(opportunityId)) {
    return
  }

  favoriteLoadingIds.value.push(opportunityId)
  const wasFavorite = isFavorite(opportunityId)

  try {
    const response = await $fetch<FavoriteOpportunityResponse[] | AddFavoritesBulkResponse>(
      `/applicants/me/favorites/opportunities/${opportunityId}`,
      {
        baseURL: config.public.apiBase,
        method: wasFavorite ? 'DELETE' : 'POST',
        headers: {
          Authorization: `Bearer ${tokenCookie.value}`,
        },
      },
    )

    const updatedIds = extractFavoriteIds(response)
    if (updatedIds.length > 0) {
      favoriteIds.value = updatedIds
      favoriteSignatures.value = []
    } else if (wasFavorite) {
      favoriteIds.value = favoriteIds.value.filter((id) => id !== opportunityId)
    } else {
      favoriteIds.value = [...favoriteIds.value, opportunityId]
    }
  } catch (error) {
    console.error('Failed to toggle favorite', error)
  } finally {
    favoriteLoadingIds.value = favoriteLoadingIds.value.filter((id) => id !== opportunityId)
  }
}

const loadMoreCards = () => {
  visibleCardsCount.value += PAGE_SIZE
}

watch(searchQuery, () => {
  visibleCardsCount.value = PAGE_SIZE
})

watch(tokenCookie, loadFavorites, { immediate: true })
watch(miniCards, applyFavoriteSignatures)
</script>

<template>
  <div class="opportunities-section container">
    <div v-if="pending && visibleCards.length === 0" class="opportunities-section__loading">
      <div class="opportunities-section__loading-track">
        <div class="opportunities-section__loading-bar" />
      </div>
      <span class="opportunities-section__loading-label">Загружаем возможности...</span>
    </div>

    <div v-else class="opportunities-section__grid">
      <div v-for="card in visibleCards" :key="card.id" class="opportunities-section__item">
        <BaseOpportunityCard
          class="opportunities-section__card"
          :id="card.id"
          :type="card.type"
          :title="card.title"
          :description="card.description"
          :format="card.format"
          :tags="card.tags"
          :employer-name="card.employerName"
          :media="card.media"
          :is-favorite="isFavorite(card.id)"
          :is-favorite-loading="isFavoriteLoading(card.id)"
          @toggle-favorite="toggleFavorite"
        />
      </div>
    </div>

    <div class="opportunities-section__load-more">
      <BaseAppButton v-if="hasMoreCards" variant="secondary" @click="loadMoreCards">
        Показать ещё
      </BaseAppButton>

      <div v-else-if="showEmptyState" class="opportunities-section__end">
        <NuxtIcon name="material-symbols:sentiment-sad-outline-rounded" size="26px" />
        <span>Ничего не найдено</span>
      </div>

      <div v-else-if="showEndState" class="opportunities-section__end">
        <NuxtIcon name="material-symbols:sentiment-sad-outline-rounded" size="26px" />
        <span>Больше ничего нет</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.opportunities-section {
  padding: 24px 0;

  &__grid {
    columns: 3;
    column-gap: 40px;
  }

  &__item {
    break-inside: avoid;
    margin-bottom: 40px;
  }

  &__load-more {
    display: flex;
    justify-content: center;
    margin-top: 12px;
  }

  &__end {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--text-tertiary-color);
    font-family: 'Inter', sans-serif;
    font-size: 14px;
    font-weight: 600;
  }

  &__loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    padding: 32px 0;
  }

  &__loading-track {
    width: min(420px, 100%);
    height: 8px;
    border-radius: 999px;
    background-color: var(--background-tertiary-color);
    overflow: hidden;
  }

  &__loading-bar {
    width: 35%;
    height: 100%;
    border-radius: inherit;
    background-color: var(--secondary-color);
    animation: loading-slide 1.15s ease-in-out infinite;
  }

  &__loading-label {
    color: var(--text-tertiary-color);
    font-family: 'Inter', sans-serif;
    font-size: 14px;
    font-weight: 600;
  }
}

@keyframes loading-slide {
  0% {
    transform: translateX(-120%);
  }
  100% {
    transform: translateX(320%);
  }
}

@media (max-width: 1200px) {
  .opportunities-section__grid {
    columns: 2;
  }
}

@media (max-width: 768px) {
  .opportunities-section__grid {
    columns: 1;
  }
}
</style>
