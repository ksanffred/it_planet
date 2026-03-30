<script setup lang="ts">
import type { Tag } from '~/types'

const route = useRoute()
const router = useRouter()
const config = useRuntimeConfig()

const emit = defineEmits<{
  'update:showMap': [value: boolean]
}>()

const showMap = ref(false)

const internshipOpportunity = ref(false)
const eventOpportunity = ref(false)
const vacansyOpportunity = ref(false)

const searchText: Ref<string> = ref('')
const searchTags: Ref<Tag[]> = ref([])

const { data: tags, error } = await useFetch<Tag[]>('/tags', {
  baseURL: config.public.apiBase,
  method: 'GET',
  lazy: true,
})

const initFromUrl = () => {
  const searchParam = route.query.search as string
  if (!searchParam || !tags.value) return

  const searchParts = searchParam.split(' ')

  const selectedTagNames: string[] = []
  const searchTextParts: string[] = []

  searchParts.forEach((part) => {
    if (part === 'стажировка') {
      internshipOpportunity.value = true
      return
    }
    if (part === 'мероприятие') {
      eventOpportunity.value = true
      return
    }
    if (part === 'вакансия') {
      vacansyOpportunity.value = true
      return
    }

    const matchingTag = tags.value!.find((t) => t.name === part)
    if (matchingTag) {
      selectedTagNames.push(part)
    } else {
      searchTextParts.push(part)
    }
  })

  if (selectedTagNames.length > 0) {
    searchTags.value = selectedTagNames
      .map((name) => tags.value!.find((t) => t.name === name))
      .filter((t): t is Tag => t !== undefined)
  }

  searchText.value = searchTextParts.join(' ')
}

watch(tags, initFromUrl, { immediate: true })

const buildSearchQuery = () => {
  const parts: string[] = []

  if (internshipOpportunity.value) parts.push('стажировка')
  if (eventOpportunity.value) parts.push('мероприятие')
  if (vacansyOpportunity.value) parts.push('вакансия')

  const tagNames = searchTags.value.map((t) => t.name)
  if (tagNames.length > 0) parts.push(...tagNames)

  const text = searchText.value.trim()
  if (text) parts.push(text)

  return parts.join(' ') || undefined
}

const updateQuery = () => {
  const search = buildSearchQuery()
  router.push({ query: { ...route.query, search: search || undefined } })
}

const submitSearch = () => {
  updateQuery()
}

watch([internshipOpportunity, eventOpportunity, vacansyOpportunity, searchTags], updateQuery, {
  deep: true,
})

const toggleTag = (tag: Tag) => {
  const index = searchTags.value.findIndex((t) => t.id === tag.id)
  if (index === -1) {
    searchTags.value.push(tag)
  } else {
    searchTags.value.splice(index, 1)
  }
}

const toggleMapView = () => {
  showMap.value = !showMap.value
  emit('update:showMap', showMap.value)
}

const tagsContainerRef = ref<HTMLElement | null>(null)
const visibleTags = ref<Tag[]>([])
const hiddenTagsCount = ref(0)

const computeVisibleTags = () => {
  if (!tagsContainerRef.value) return

  const container = tagsContainerRef.value
  const containerWidth = container.offsetWidth

  const tagsElements = container.querySelectorAll<HTMLDivElement>('.search-section__tag-item')

  if (!tagsElements || tagsElements.length === 0) {
    visibleTags.value = [...searchTags.value]
    hiddenTagsCount.value = 0
    return
  }

  const buttonWidth = 120
  const edgeBuffer = 16
  const availableWidth = containerWidth - buttonWidth - edgeBuffer

  let totalWidth = 0
  let visibleCount = 0
  const gap = 8

  for (let i = 0; i < tagsElements.length; i++) {
    const element = tagsElements[i]
    if (!element) break

    const tagWidth = element.offsetWidth + gap
    if (totalWidth + tagWidth <= availableWidth) {
      totalWidth += tagWidth
      visibleCount++
    } else {
      break
    }
  }

  if (visibleCount >= searchTags.value.length) {
    visibleTags.value = [...searchTags.value]
    hiddenTagsCount.value = 0
  } else {
    const moreElement = container.querySelector<HTMLDivElement>('.search-section__tag-more')
    const moreWidth = moreElement ? moreElement.offsetWidth + gap : 0

    let adjustedCount = visibleCount
    while (adjustedCount > 0 && totalWidth + moreWidth > availableWidth) {
      adjustedCount--
      totalWidth -= (tagsElements[adjustedCount]?.offsetWidth || 0) + gap
    }

    visibleTags.value = searchTags.value.slice(0, adjustedCount)
    hiddenTagsCount.value = searchTags.value.length - adjustedCount
  }
}

watch(
  searchTags,
  (newTags) => {
    visibleTags.value = [...newTags]
    hiddenTagsCount.value = 0
    nextTick(() => {
      computeVisibleTags()
    })
  },
  { deep: true, immediate: true },
)

onMounted(() => {
  window.addEventListener('resize', computeVisibleTags)
})

onUnmounted(() => {
  window.removeEventListener('resize', computeVisibleTags)
})
</script>

<template>
  <div class="search-section container">
    <div class="search-section__types-row">
      <BaseAppButton
        @click="internshipOpportunity = !internshipOpportunity"
        variant="primary"
        class="search-section__type-button search-section__type-button--green"
      >
        Стажерские программы
        <NuxtIcon v-if="internshipOpportunity" name="material-symbols:close-rounded" size="20px" />
      </BaseAppButton>
      <BaseAppButton
        @click="eventOpportunity = !eventOpportunity"
        variant="primary"
        class="search-section__type-button"
      >
        Карьерные мероприятия
        <NuxtIcon v-if="eventOpportunity" name="material-symbols:close-rounded" size="20px" />
      </BaseAppButton>
      <BaseAppButton
        @click="vacansyOpportunity = !vacansyOpportunity"
        variant="secondary"
        class="search-section__type-button"
      >
        Вакансии
        <NuxtIcon v-if="vacansyOpportunity" name="material-symbols:close-rounded" size="20px" />
      </BaseAppButton>
    </div>

    <div class="search-section__tags-row" ref="tagsContainerRef">
      <div class="search-section__tags-list">
        <div
          v-for="tag in visibleTags"
          :key="tag.id"
          class="search-section__tag-item"
          @click="toggleTag(tag)"
        >
          <BaseAppTag
            :text-color="'var(--text-primary-color)'"
            :bordered="true"
            class="search-section__tag"
          >
            {{ tag.name }}
          </BaseAppTag>
        </div>
        <div v-if="hiddenTagsCount > 0" class="search-section__tag-item search-section__tag-more">
          <BaseAppTag
            :text-color="'var(--text-primary-color)'"
            :bordered="true"
            class="search-section__tag"
          >
            и ещё {{ hiddenTagsCount }}
          </BaseAppTag>
        </div>
      </div>
      <BaseTagSelector v-model:selected-tags="searchTags" :available-tags="tags || []" />
    </div>

    <div class="search-section__search-row container">
      <BaseAppInput
        v-model="searchText"
        placeholder="Поиск стажировок, компаний, технологий..."
        @keydown.enter.prevent="submitSearch"
      />
      <BaseAppButton variant="secondary" class="search-section__map-button" @click="toggleMapView">
        <img
          class="search-section__map-thumb"
          src="/media/images/mapThumb.webp"
          alt="Map demonstration"
        />
        <div class="search-section__text">
          <span class="search-section__text-title">
            {{ showMap ? 'Закрыть карту' : 'Открыть карту' }}
          </span>
          <span class="search-section__text-subtitle">Смотрите возможности по локации</span>
        </div>
      </BaseAppButton>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.search-section {
  display: flex;
  flex-direction: column;

  gap: 12px;

  &__types-row {
    display: flex;
    gap: 12px;
  }

  &__type-button {
    flex: 1;

    &--green {
      background-color: var(--tertiary-color);
    }
  }

  &__tags-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    min-height: 40px;
  }

  &__tags-list {
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
    gap: 8px;
    flex: 1;
    overflow: hidden;
  }

  &__tag-item {
    display: flex;
    align-items: center;
    gap: 6px;
    flex-shrink: 0;
    cursor: pointer;

    .search-section__tag {
      cursor: pointer;
    }
  }

  &__tag-more {
    .search-section__tag {
      font-style: italic;
    }
  }

  &__search-row {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    width: 100%;
  }

  &__map-button {
    display: flex;
    align-items: center;
    min-width: fit-content;
    color: var(--text-primary-color) !important;
    background-color: var(--background-tertiary-color);
    border-radius: 20px;
    gap: 10px;
    padding-block: 6px;
    padding-inline: 10px;
  }

  &__map-thumb {
    border-radius: 12px;
    border: 1px solid var(--text-primary-color);

    .dark & {
      border: 1px solid var(--border-color);
    }
  }

  &__text {
    display: flex;
    text-align: left;
    text-wrap: nowrap;
    flex-direction: column;
    gap: 4px;

    &-title {
      font-family: 'Plus Jakarta Sans', sans-serif;
      font-size: 16px;
      font-weight: 800;
    }

    &-subtitle {
      font-family: 'Inter', sans-serif;
      font-size: 12px;
      font-weight: 500;
    }
  }
}
</style>
