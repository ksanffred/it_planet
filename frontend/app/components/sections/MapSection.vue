<script setup lang="ts">
import type {
  AddFavoritesBulkResponse,
  FavoriteOpportunityResponse,
  OpportunityMiniCard,
} from '@/types'

type YMapCoordinates = [number, number]

type YMapEntity = {
  update?: (props: unknown) => void
}

type YMapInstance = {
  addChild: (child: unknown) => unknown
  removeChild: (child: unknown) => unknown
  update?: (props: unknown) => void
  destroy?: () => void
}

type YMapFeature = {
  type: 'Feature'
  id: number
  geometry: {
    type: 'Point'
    coordinates: YMapCoordinates
  }
  properties: {
    id: number
    title: string
    employerName: string
    format: OpportunityMiniCard['format']
    city: string
    type: OpportunityMiniCard['type']
  }
}

type YMapClustererEntity = YMapEntity

type YMapClustererModule = {
  YMapClusterer: new (props: {
    method: unknown
    features: YMapFeature[]
    marker: (feature: YMapFeature) => YMapEntity
    cluster: (coordinates: YMapCoordinates, features: YMapFeature[]) => YMapEntity
  }) => YMapClustererEntity
  clusterByGrid: (props: { gridSize: number }) => unknown
}

type YMaps3Instance = {
  ready: Promise<void>
  import: ((pkg: string) => Promise<unknown>) & {
    registerCdn?: (baseUrl: string, packageName: string) => void
  }
  YMap: new (container: HTMLElement, props: unknown) => YMapInstance
  YMapMarker: new (
    props: { coordinates: YMapCoordinates; source: string },
    element?: HTMLElement,
  ) => YMapEntity
  YMapLayer: new (props: { source: string; type: 'markers'; zIndex?: number }) => YMapEntity
  YMapFeatureDataSource: new (props: { id: string }) => YMapEntity
  YMapDefaultSchemeLayer: new (props?: unknown) => unknown
  YMapDefaultFeaturesLayer: new (props?: unknown) => unknown
}

const MAP_CENTER: YMapCoordinates = [37.617635, 55.755814]
const MAP_SOURCE_ID = 'opportunity-markers-source'
const CLUSTER_PACKAGE = '@yandex/ymaps3-clusterer@0.0.1'

const config = useRuntimeConfig()
const route = useRoute()
const tokenCookie = useCookie<string | null>('auth_token')

const searchQuery = computed(() => {
  const value = route.query.search
  return typeof value === 'string' ? value.trim() : ''
})

const { data: miniCards, pending } = await useFetch<OpportunityMiniCard[]>(
  '/opportunities/mini-cards',
  {
    baseURL: config.public.apiBase,
    method: 'GET',
    lazy: true,
    query: computed(() => ({
      search: searchQuery.value || undefined,
    })),
    watch: [searchQuery],
  },
)

const mapContainerRef = ref<HTMLElement | null>(null)
const mapError = ref('')
const selectedCardId = ref<number | null>(null)
const selectedClusterCardIds = ref<number[]>([])
const favoriteIds = ref<number[]>([])
const favoriteLoadingIds = ref<number[]>([])
const favoriteSignatures = ref<string[]>([])

let ymaps3Instance: YMaps3Instance | null = null
let clustererModule: YMapClustererModule | null = null
let mapInstance: YMapInstance | null = null
let clustererEntity: YMapClustererEntity | null = null

type FavoriteCardLookup = {
  id?: number
  opportunityId?: number
  title?: string
  company_name?: string
  companyName?: string
  type?: string
}

const waitForYMaps = (timeoutMs = 10000, intervalMs = 120) =>
  new Promise<YMaps3Instance>((resolve, reject) => {
    const start = Date.now()
    const timerId = window.setInterval(() => {
      const ymaps3 = (window as Window & { ymaps3?: YMaps3Instance }).ymaps3
      if (ymaps3) {
        window.clearInterval(timerId)
        resolve(ymaps3)
        return
      }

      if (Date.now() - start >= timeoutMs) {
        window.clearInterval(timerId)
        reject(new Error('Yandex Maps script is not loaded'))
      }
    }, intervalMs)
  })

const formatLabel: Record<OpportunityMiniCard['format'], string> = {
  OFFICE: 'Офис',
  HYBRID: 'Гибрид',
  REMOTE: 'Удаленно',
}

const typeLabel: Record<OpportunityMiniCard['type'], string> = {
  VACANCY: 'Вакансия',
  INTERNSHIP: 'Стажировка',
  EVENT: 'Мероприятие',
  MENTORSHIP: 'Менторство',
}

const normalizeValue = (value: string | undefined) => (value ?? '').trim().toLowerCase()
const buildFavoriteSignature = (
  title: string,
  companyName: string,
  type: OpportunityMiniCard['type'],
) => `${normalizeValue(title)}|${normalizeValue(companyName)}|${normalizeValue(type)}`

const isFavorite = (id: number) => favoriteIds.value.includes(id)
const isFavoriteLoading = (id: number) => favoriteLoadingIds.value.includes(id)

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
      return buildFavoriteSignature(
        typedItem.title,
        company,
        typedItem.type as OpportunityMiniCard['type'],
      )
    })
    .filter((signature): signature is string => Boolean(signature))
}

const applyFavoriteSignatures = () => {
  if (favoriteSignatures.value.length === 0 || !miniCards.value?.length) {
    return
  }

  const signatures = new Set(favoriteSignatures.value)
  favoriteIds.value = miniCards.value
    .filter((card) =>
      signatures.has(buildFavoriteSignature(card.title, card.employerName, card.type)),
    )
    .map((card) => card.id)
}

const loadFavorites = async () => {
  if (!tokenCookie.value) {
    favoriteIds.value = []
    favoriteSignatures.value = []
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

const toggleFavorite = async (opportunityId: number) => {
  if (!tokenCookie.value) {
    navigateTo('/auth/login')
    return
  }

  if (isFavoriteLoading(opportunityId)) return

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

    const ids = extractFavoriteIds(response)
    if (ids.length > 0) {
      favoriteIds.value = ids
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

const createMarkerElement = (feature: YMapFeature) => {
  const element = document.createElement('button')
  element.type = 'button'
  element.className = 'map-section__marker'
  element.classList.add(`map-section__marker--${feature.properties.type.toLowerCase()}`)
  if (isFavorite(feature.properties.id)) {
    element.classList.add('map-section__marker--favorite')
  }
  if (selectedCardId.value === feature.properties.id) {
    element.classList.add('map-section__marker--active')
  }
  const popup = document.createElement('div')
  popup.className = 'map-section__popup'

  const popupTitle = document.createElement('p')
  popupTitle.className = 'map-section__popup-title'
  popupTitle.textContent = `${feature.properties.title} @ ${feature.properties.employerName}`

  const popupMeta = document.createElement('p')
  popupMeta.className = 'map-section__popup-meta'
  popupMeta.textContent = `${formatLabel[feature.properties.format]} · ${feature.properties.city} · ${typeLabel[feature.properties.type]}`

  popup.appendChild(popupTitle)
  popup.appendChild(popupMeta)
  element.appendChild(popup)

  element.title = `${feature.properties.title} @ ${feature.properties.employerName}`
  element.setAttribute('aria-label', `Показать карточку: ${feature.properties.title}`)
  element.addEventListener('click', (event) => {
    event.preventDefault()
    event.stopPropagation()
    selectedClusterCardIds.value = []
    selectedCardId.value = feature.properties.id
  })

  return element
}

const getClusterSizeClass = (count: number) => {
  if (count >= 25) return 'map-section__cluster--lg'
  if (count >= 10) return 'map-section__cluster--md'
  return 'map-section__cluster--sm'
}

const getDominantType = (features: YMapFeature[]) => {
  const score = features.reduce<Record<OpportunityMiniCard['type'], number>>(
    (acc, feature) => {
      acc[feature.properties.type] += 1
      return acc
    },
    {
      VACANCY: 0,
      INTERNSHIP: 0,
      EVENT: 0,
      MENTORSHIP: 0,
    },
  )

  return (Object.entries(score).sort((a, b) => b[1] - a[1])[0]?.[0] ??
    'VACANCY') as OpportunityMiniCard['type']
}

const createClusterElement = (
  count: number,
  dominantType: OpportunityMiniCard['type'],
  hasFavorite: boolean,
  onClick: () => void,
) => {
  const element = document.createElement('button')
  element.type = 'button'
  element.className = 'map-section__cluster'
  element.classList.add(getClusterSizeClass(count))
  element.classList.add(`map-section__cluster--${dominantType.toLowerCase()}`)
  if (hasFavorite) {
    element.classList.add('map-section__cluster--favorite')
  }
  element.textContent = String(count)
  element.setAttribute('aria-label', `Кластер: ${count} точек`)
  element.addEventListener('click', (event) => {
    event.preventDefault()
    event.stopPropagation()
    onClick()
  })

  return element
}

const resetMapView = () => {
  mapInstance?.update?.({
    location: {
      center: MAP_CENTER,
      zoom: 10,
      duration: 240,
    },
  })
}

const removeClusterer = () => {
  if (!mapInstance || !clustererEntity) return
  mapInstance.removeChild(clustererEntity)
  clustererEntity = null
}

const cardsToFeatures = (cards: OpportunityMiniCard[]): YMapFeature[] =>
  cards
    .filter((card) => Number.isFinite(card.lat) && Number.isFinite(card.lng))
    .map((card) => ({
      type: 'Feature',
      id: card.id,
      geometry: {
        type: 'Point',
        coordinates: [card.lng, card.lat],
      },
      properties: {
        id: card.id,
        title: card.title,
        employerName: card.employerName,
        format: card.format,
        city: card.city,
        type: card.type,
      },
    }))

const updateMapCenter = (features: YMapFeature[]) => {
  if (!mapInstance || features.length === 0) {
    resetMapView()
    return
  }

  const center = features.reduce(
    (acc, feature) => {
      acc.lng += feature.geometry.coordinates[0]
      acc.lat += feature.geometry.coordinates[1]
      return acc
    },
    { lng: 0, lat: 0 },
  )

  mapInstance.update?.({
    location: {
      center: [center.lng / features.length, center.lat / features.length],
      zoom: features.length > 1 ? 8 : 12,
      duration: 240,
    },
  })
}

const renderClusterer = (options?: { recenter?: boolean }) => {
  if (!mapInstance || !ymaps3Instance || !clustererModule) return

  removeClusterer()

  const features = cardsToFeatures(miniCards.value ?? [])
  if (features.length === 0) {
    resetMapView()
    return
  }

  const clusterer = new clustererModule.YMapClusterer({
    method: clustererModule.clusterByGrid({ gridSize: 64 }),
    features,
    marker: (feature) =>
      new ymaps3Instance!.YMapMarker(
        { coordinates: feature.geometry.coordinates, source: MAP_SOURCE_ID },
        createMarkerElement(feature),
      ),
    cluster: (coordinates, clusteredFeatures) => {
      const dominantType = getDominantType(clusteredFeatures)
      const hasFavorite = clusteredFeatures.some((feature) => isFavorite(feature.properties.id))
      const clusterIds = clusteredFeatures.map((feature) => feature.properties.id)

      return new ymaps3Instance!.YMapMarker(
        { coordinates, source: MAP_SOURCE_ID },
        createClusterElement(clusteredFeatures.length, dominantType, hasFavorite, () => {
          selectedCardId.value = null
          selectedClusterCardIds.value = clusterIds
        }),
      )
    },
  })

  clustererEntity = clusterer
  mapInstance.addChild(clusterer)

  if (options?.recenter !== false) {
    updateMapCenter(features)
  }
}

const initMap = async () => {
  if (!mapContainerRef.value) return

  try {
    const ymaps3 = await waitForYMaps()
    await ymaps3.ready
    ymaps3Instance = ymaps3

    ymaps3.import.registerCdn?.('https://cdn.jsdelivr.net/npm/{package}', CLUSTER_PACKAGE)
    clustererModule = (await ymaps3.import(CLUSTER_PACKAGE)) as YMapClustererModule

    mapInstance = new ymaps3.YMap(mapContainerRef.value, {
      location: {
        center: MAP_CENTER,
        zoom: 10,
      },
    })

    mapInstance.addChild(new ymaps3.YMapDefaultSchemeLayer())
    mapInstance.addChild(new ymaps3.YMapDefaultFeaturesLayer())
    mapInstance.addChild(new ymaps3.YMapFeatureDataSource({ id: MAP_SOURCE_ID }))
    mapInstance.addChild(
      new ymaps3.YMapLayer({
        source: MAP_SOURCE_ID,
        type: 'markers',
        zIndex: 1800,
      }),
    )

    renderClusterer({ recenter: true })
  } catch (error) {
    mapError.value = 'Не удалось загрузить карту'
    console.error('Failed to initialize yandex map', error)
  }
}

const selectedCard = computed(
  () => (miniCards.value ?? []).find((card) => card.id === selectedCardId.value) ?? null,
)
const isEmptySearchResult = computed(() => !pending.value && (miniCards.value?.length ?? 0) === 0)
const selectedClusterCards = computed(() => {
  if (selectedClusterCardIds.value.length === 0) return []

  const byId = new Map((miniCards.value ?? []).map((card) => [card.id, card]))
  return selectedClusterCardIds.value
    .map((id) => byId.get(id))
    .filter((card): card is OpportunityMiniCard => Boolean(card))
})

watch(
  miniCards,
  (cards) => {
    const nextCards = cards ?? []
    if (nextCards.length === 0) {
      selectedCardId.value = null
      selectedClusterCardIds.value = []
      renderClusterer({ recenter: true })
      return
    }

    if (
      selectedCardId.value !== null &&
      !nextCards.some((card) => card.id === selectedCardId.value)
    ) {
      selectedCardId.value = null
    }

    if (selectedClusterCardIds.value.length > 0) {
      const nextIds = new Set(nextCards.map((card) => card.id))
      selectedClusterCardIds.value = selectedClusterCardIds.value.filter((id) => nextIds.has(id))
    }

    renderClusterer({ recenter: true })
  },
  { immediate: true },
)

watch(selectedCardId, () => {
  renderClusterer({ recenter: false })
})

watch(favoriteIds, () => {
  renderClusterer({ recenter: false })
})
watch(tokenCookie, loadFavorites, { immediate: true })
watch(miniCards, applyFavoriteSignatures)

onMounted(initMap)

onUnmounted(() => {
  removeClusterer()
  mapInstance?.destroy?.()
  mapInstance = null
  ymaps3Instance = null
  clustererModule = null
})
</script>

<template>
  <section class="map-section">
    <div class="map-section__layout">
      <div class="map-section__map-wrap">
        <div ref="mapContainerRef" class="map-section__canvas bordered" />
        <div v-if="pending" class="map-section__loading">Загружаем точки...</div>
      </div>

      <aside v-if="selectedCard" class="map-section__side">
        <BaseOpportunityCard
          class="map-section__side-card"
          :id="selectedCard.id"
          :media="selectedCard.media"
          :title="selectedCard.title"
          :description="selectedCard.description"
          :employer-name="selectedCard.employerName"
          :format="selectedCard.format"
          :type="selectedCard.type"
          :city="selectedCard.city"
          :address="selectedCard.address"
          :lat="selectedCard.lat"
          :lng="selectedCard.lng"
          :tags="selectedCard.tags"
          :is-favorite="isFavorite(selectedCard.id)"
          :is-favorite-loading="isFavoriteLoading(selectedCard.id)"
          @toggle-favorite="toggleFavorite"
        />
      </aside>
      <aside v-else-if="selectedClusterCards.length > 0" class="map-section__side bordered">
        <div class="map-section__cluster-list">
          <p class="map-section__cluster-list-title">
            Возможности в кластере: {{ selectedClusterCards.length }}
          </p>
          <div class="map-section__cluster-list-items">
            <BaseOpportunityCard
              v-for="card in selectedClusterCards"
              :key="card.id"
              class="map-section__cluster-card"
              :id="card.id"
              :media="card.media"
              :title="card.title"
              :description="card.description"
              :employer-name="card.employerName"
              :format="card.format"
              :type="card.type"
              :city="card.city"
              :address="card.address"
              :lat="card.lat"
              :lng="card.lng"
              :tags="card.tags"
              :is-favorite="isFavorite(card.id)"
              :is-favorite-loading="isFavoriteLoading(card.id)"
              @toggle-favorite="toggleFavorite"
            />
          </div>
        </div>
      </aside>
      <aside v-else class="map-section__side map-section__side--empty bordered">
        <p class="map-section__empty-text">
          {{
            isEmptySearchResult
              ? 'По вашему запросу ничего не найдено'
              : 'Выберите возможность на карте'
          }}
        </p>
      </aside>
    </div>

    <div v-if="mapError" class="map-section__error">
      {{ mapError }}
    </div>
  </section>
</template>

<style scoped lang="scss">
.map-section {
  position: relative;
  margin-top: 20px;
  margin-bottom: 24px;

  &__layout {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 360px;
    gap: 16px;
    align-items: start;
  }

  &__map-wrap {
    position: relative;
  }

  &__canvas {
    width: 100%;
    height: 620px;
    border-radius: 24px;
    overflow: hidden;
  }

  &__side {
    position: sticky;
    top: 16px;
    height: 620px;
    display: flex;
    border-radius: 24px;
    overflow: hidden;
  }

  &__side--empty {
    align-items: center;
    justify-content: center;
    border-radius: 24px;
    background-color: var(--background-secondary-color);
  }

  &__side-card {
    width: 100%;
    height: 100%;
  }

  &__cluster-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
    width: 100%;
    height: 100%;
    padding: 12px;

    &-title {
      margin: 0;
      font-family: 'Plus Jakarta Sans', sans-serif;
      font-size: 16px;
      font-weight: 800;
      color: var(--text-inverted-color);
    }

    &-items {
      display: flex;
      flex-direction: column;
      gap: 10px;
      overflow: auto;
      min-height: 0;
      padding-right: 4px;
    }
  }

  &__cluster-card {
    width: 100%;
  }

  &__empty-text {
    margin: 0;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-size: 22px;
    font-weight: 800;
    text-align: center;
    color: var(--text-inverted-color);
  }

  &__error {
    position: absolute;
    top: 16px;
    left: 16px;
    padding: 8px 12px;
    border-radius: 12px;
    background-color: var(--background-secondary-color);
    color: var(--text-inverted-color);
    font-family: 'Inter', sans-serif;
    font-size: 13px;
    font-weight: 600;
  }

  &__loading {
    position: absolute;
    top: 16px;
    right: 16px;
    padding: 8px 12px;
    border-radius: 12px;
    background-color: var(--background-secondary-color);
    color: var(--text-inverted-color);
    font-family: 'Inter', sans-serif;
    font-size: 13px;
    font-weight: 600;
  }

  :deep(.map-section__marker) {
    width: 18px;
    height: 18px;
    border-radius: 50%;
    border: 3px solid var(--background-primary-color);
    background-color: var(--secondary-color);
    box-shadow: 0 0 0 4px rgb(0 0 0 / 12%);
    cursor: pointer;
    padding: 0;
    position: relative;
  }

  :deep(.map-section__marker--active) {
    transform: scale(1.22);
    box-shadow: 0 0 0 6px rgb(255 255 255 / 40%);
  }

  :deep(.map-section__marker--favorite) {
    border-color: #f8d34a;
    box-shadow:
      0 0 0 4px rgb(248 211 74 / 30%),
      0 0 0 8px rgb(248 211 74 / 14%);
  }

  :deep(.map-section__marker:hover .map-section__popup),
  :deep(.map-section__marker:focus-visible .map-section__popup) {
    opacity: 1;
    visibility: visible;
    transform: translate(-50%, -8px);
  }

  :deep(.map-section__popup) {
    position: absolute;
    left: 50%;
    bottom: 100%;
    transform: translate(-50%, 0);
    min-width: 320px;
    max-width: 380px;
    border-radius: 16px;
    border: 2px solid var(--border-color);
    background-color: var(--background-secondary-color);
    color: var(--text-inverted-color);
    padding: 14px 16px;
    text-align: left;
    pointer-events: none;
    box-shadow: 0 14px 30px rgb(0 0 0 / 20%);
    opacity: 0;
    visibility: hidden;
    transition:
      opacity 0.16s ease,
      transform 0.16s ease,
      visibility 0.16s ease;
    z-index: 2200;
  }

  :deep(.map-section__popup-title) {
    margin: 0 0 6px;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 800;
    font-size: 16px;
    line-height: 1.2;
  }

  :deep(.map-section__popup-meta) {
    margin: 0;
    font-family: 'Inter', sans-serif;
    font-size: 13px;
    font-weight: 600;
    line-height: 1.3;
    color: var(--text-inverted-color);
    opacity: 0.9;
  }

  :deep(.map-section__marker--vacancy) {
    background-color: #2f3a4a;
    border-color: #ffffff;
  }

  :deep(.map-section__marker--internship) {
    background-color: var(--tertiary-color);
  }

  :deep(.map-section__marker--event) {
    background-color: var(--primary-color);
  }

  :deep(.map-section__marker--mentorship) {
    background-color: var(--secondary-color);
  }

  :deep(.map-section__cluster) {
    min-width: 34px;
    height: 34px;
    border-radius: 999px;
    border: 3px solid var(--background-primary-color);
    background-color: var(--secondary-color);
    color: var(--text-primary-color);
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-size: 12px;
    font-weight: 800;
    line-height: 1;
    padding: 0 7px;
    box-shadow: 0 0 0 6px rgb(0 0 0 / 14%);
    cursor: default;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(1px);
  }

  :deep(.map-section__cluster--sm) {
    min-width: 34px;
    height: 34px;
    font-size: 12px;
  }

  :deep(.map-section__cluster--md) {
    min-width: 40px;
    height: 40px;
    font-size: 13px;
  }

  :deep(.map-section__cluster--lg) {
    min-width: 48px;
    height: 48px;
    font-size: 14px;
    font-weight: 900;
  }

  :deep(.map-section__cluster--vacancy) {
    background-color: #2f3a4a;
    color: #ffffff;
  }

  :deep(.map-section__cluster--internship) {
    background-color: var(--tertiary-color);
  }

  :deep(.map-section__cluster--event) {
    background-color: var(--primary-color);
  }

  :deep(.map-section__cluster--mentorship) {
    background-color: var(--secondary-color);
  }

  :deep(.map-section__cluster--favorite) {
    border-color: #f8d34a;
    box-shadow:
      0 0 0 6px rgb(248 211 74 / 30%),
      0 0 0 10px rgb(248 211 74 / 16%);
  }
}

:global(.dark) .map-section :deep(.map-section__marker--vacancy) {
  background-color: #c3d1e6;
  border-color: #0f1726;
}

:global(.dark) .map-section :deep(.map-section__cluster--vacancy) {
  background-color: #c3d1e6;
  color: #0f1726;
  border-color: #0f1726;
}

@media (max-width: 768px) {
  .map-section__layout {
    grid-template-columns: 1fr;
  }

  .map-section__canvas {
    height: 440px;
  }

  .map-section__side {
    position: static;
    height: auto;
  }
}
</style>
