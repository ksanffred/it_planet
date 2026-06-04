<script lang="ts" setup>
import type { CreateOpportunityRequest, MediaUploadResponse, OpportunityFormat, OpportunityType, Tag } from '~/types'
import type { FetchError } from 'ofetch'
import { normalizeStorageAssetUrl } from '~/utils/normalizeStorageAssetUrl'

type EmployerMeResponse = {
  id: number
  companyName?: string
}

type CreatedOpportunityResponse = {
  id?: number
}

type EditSection = 'title' | 'description' | 'tags' | 'format' | 'place' | 'date' | null

const config = useRuntimeConfig()
const tokenCookie = useCookie<string | null>('auth_token')

if (!tokenCookie.value) {
  await navigateTo('/auth/login?redirect=/opportunities/create')
}

const authHeaders = { Authorization: `Bearer ${tokenCookie.value}` }

const activeModal = ref<EditSection>(null)
const editTitle = ref('')
const editDescription = ref('')
const editFormat = ref<OpportunityFormat>('REMOTE')
const editCity = ref('')
const editAddress = ref('')
const editSalaryFrom = ref<string>('')
const editSalaryTo = ref<string>('')
const editExpiresAt = ref('')
const editTags = ref<Tag[]>([])
const availableTags = ref<Tag[]>([])
const opportunityType = ref<OpportunityType>('VACANCY')
const isSubmitting = ref(false)
const isGeocoding = ref(false)
const error = ref('')

const uploadedMedia = ref<MediaUploadResponse[]>([])
const isUploadingMedia = ref(false)
const mediaUploadError = ref('')
const fileInput = ref<HTMLInputElement | null>(null)

const { data: employer, error: employerError } = await useFetch<EmployerMeResponse>(
  '/employers/me',
  { baseURL: config.public.apiBase, method: 'GET', headers: authHeaders },
)

watchEffect(() => {
  if (employerError.value?.statusCode === 404) navigateTo('/employers/register')
  else if (employerError.value?.statusCode === 401)
    navigateTo('/auth/login?redirect=/opportunities/create')
})

const { data: tagsData } = await useFetch<Tag[]>('/tags', {
  baseURL: config.public.apiBase,
  method: 'GET',
  default: () => [],
})

watchEffect(() => {
  availableTags.value = tagsData.value ?? []
})

const showSalary = computed(() => opportunityType.value !== 'EVENT')

const titleRowBg = computed(() => {
  const map: Record<OpportunityType, string> = {
    VACANCY: '#374151',
    INTERNSHIP: 'var(--tertiary-color)',
    MENTORSHIP: '#7c3aed',
    EVENT: 'var(--primary-color)',
  }
  return map[opportunityType.value]
})

const displayTitle = computed(() => editTitle.value || 'Название возможности')

const displaySalary = computed(() => {
  if (!showSalary.value || (!editSalaryFrom.value && !editSalaryTo.value)) return ''
  return [editSalaryFrom.value, editSalaryTo.value]
    .filter(Boolean)
    .map((s) => `${Number(s).toLocaleString('ru-RU')} ₽`)
    .join(' — ')
})

const displayFormatLabel = computed(() => {
  const map: Record<OpportunityFormat, string> = {
    REMOTE: 'Удалённо',
    OFFICE: 'Офис',
    HYBRID: 'Гибрид',
  }
  return map[editFormat.value] || 'Не указано'
})

const displayPlace = computed(
  () => [editCity.value, editAddress.value].filter(Boolean).join(', ') || 'Не указано',
)

const displayDate = computed(() => {
  if (!editExpiresAt.value) return 'Дата не указана'
  return `До: ${new Date(editExpiresAt.value).toLocaleDateString('ru-RU')}`
})

const displayDescription = computed(() => editDescription.value || 'Описание отсутствует')

const openEditTitle = () => {
  activeModal.value = 'title'
}
const openEditDescription = () => {
  activeModal.value = 'description'
}
const openEditTags = () => {
  activeModal.value = 'tags'
}
const openEditFormat = () => {
  activeModal.value = 'format'
}
const openEditPlace = () => {
  activeModal.value = 'place'
}
const openEditDate = () => {
  activeModal.value = 'date'
}
const closeSectionModal = () => {
  activeModal.value = null
  error.value = ''
}

const resolveGeocodeApiKey = () =>
  String(config.public.yandexMapsApiKey || '071323d3-8c9a-48c3-9baf-5c437af0f80e').trim()

const geocodeAddress = async (query: string) => {
  const apiKey = resolveGeocodeApiKey()
  let response: {
    response?: {
      GeoObjectCollection?: {
        featureMember?: Array<{
          GeoObject?: { Point?: { pos?: string } }
        }>
      }
    }
  }
  try {
    response = await $fetch('https://geocode-maps.yandex.ru/1.x/', {
      method: 'GET',
      query: { apikey: apiKey, geocode: query, format: 'json', lang: 'ru_RU', results: 1 },
    })
  } catch (geocodeError) {
    console.error('[create-opportunity] geocode failed', { query, geocodeError })
    throw geocodeError
  }
  const pos =
    response.response?.GeoObjectCollection?.featureMember?.[0]?.GeoObject?.Point?.pos ?? ''
  const [lngRaw, latRaw] = String(pos).trim().split(/\s+/)
  const lat = Number(latRaw)
  const lng = Number(lngRaw)
  if (!Number.isFinite(lat) || !Number.isFinite(lng)) return null
  return { lat, lng }
}

const toOptionalNumber = (value: string) => {
  const n = String(value ?? '').trim()
  if (!n) return undefined
  const p = Number(n)
  return Number.isFinite(p) ? p : undefined
}

const toIsoDate = (value: string) => {
  const n = String(value ?? '').trim()
  if (!n) return undefined
  return new Date(n).toISOString()
}

const triggerMediaUpload = () => fileInput.value?.click()

const uploadMedia = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    mediaUploadError.value = 'Можно загружать только изображения'
    input.value = ''
    return
  }

  isUploadingMedia.value = true
  mediaUploadError.value = ''

  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await $fetch<MediaUploadResponse>('/opportunities/media', {
      baseURL: config.public.apiBase,
      method: 'POST',
      headers: authHeaders,
      body: formData,
    })

    uploadedMedia.value.push(response)
  } catch {
    mediaUploadError.value = 'Не удалось загрузить изображение'
  } finally {
    isUploadingMedia.value = false
    input.value = ''
  }
}

const removeMedia = (index: number) => {
  uploadedMedia.value.splice(index, 1)
  if (currentMediaSlide.value >= uploadedMedia.value.length) {
    currentMediaSlide.value = Math.max(0, uploadedMedia.value.length - 1)
  }
}

const currentMediaSlide = ref(0)
const mediaSlideCount = computed(() => uploadedMedia.value.length)
const currentMediaItem = computed(() => uploadedMedia.value[currentMediaSlide.value])

const prevMediaSlide = () => {
  if (mediaSlideCount.value <= 1) return
  currentMediaSlide.value =
    (currentMediaSlide.value - 1 + mediaSlideCount.value) % mediaSlideCount.value
}

const nextMediaSlide = () => {
  if (mediaSlideCount.value <= 1) return
  currentMediaSlide.value =
    (currentMediaSlide.value + 1) % mediaSlideCount.value
}

const goToMediaSlide = (index: number) => {
  if (index < 0 || index >= mediaSlideCount.value) return
  currentMediaSlide.value = index
}

const handlePublish = async () => {
  if (isSubmitting.value) return
  error.value = ''
  if (!editTitle.value.trim()) {
    error.value = 'Укажите название возможности'
    return
  }
  if (!employer.value?.id) {
    error.value = 'Не удалось определить организацию'
    return
  }

  isSubmitting.value = true
  isGeocoding.value = true

  try {
    let coordinates: { lat: number; lng: number } | null = null
    const geocodeQuery = [editCity.value.trim(), editAddress.value.trim()]
      .filter(Boolean)
      .join(', ')
    if (geocodeQuery) {
      coordinates = await geocodeAddress(geocodeQuery)
      if (!coordinates) {
        error.value = 'Не удалось определить координаты по указанному адресу'
        return
      }
    }

    const payload: CreateOpportunityRequest = {
      employerId: employer.value.id,
      title: editTitle.value.trim(),
      description: editDescription.value.trim() || undefined,
      type: opportunityType.value,
      format: editFormat.value,
      city: editCity.value.trim() || undefined,
      address: editAddress.value.trim() || undefined,
      lat: coordinates?.lat,
      lng: coordinates?.lng,
      salaryFrom: showSalary.value ? toOptionalNumber(editSalaryFrom.value) : undefined,
      salaryTo: showSalary.value ? toOptionalNumber(editSalaryTo.value) : undefined,
      expiresAt: toIsoDate(editExpiresAt.value),
      status: 'ACTIVE' as const,
      tagIds: editTags.value.map((t) => t.id),
      media: uploadedMedia.value.map((m) => m.path || m.url),
    }

    const response = await $fetch<CreatedOpportunityResponse>('/opportunities', {
      baseURL: config.public.apiBase,
      method: 'POST',
      headers: authHeaders,
      body: payload,
    })

    if (response?.id) await navigateTo(`/opportunities/${response.id}`)
    else await navigateTo('/employers/me')
  } catch (requestError) {
    const typedError = requestError as FetchError<{ error?: string; message?: string }>
    const backendMessage =
      typedError.data?.error || typedError.data?.message || typedError.message || ''
    console.error('[create-opportunity] create failed', {
      statusCode: typedError.statusCode,
      data: typedError.data,
      requestError,
    })

    if (typedError.statusCode === 400) {
      error.value = backendMessage
        ? `Проверьте данные: ${backendMessage}`
        : 'Проверьте корректность введенных данных'
    } else if (typedError.statusCode === 401) {
      error.value = 'Требуется авторизация'
    } else if (typedError.statusCode === 403) {
      error.value = 'Недостаточно прав для создания возможности'
    } else if (typedError.statusCode === 0 || !typedError.statusCode) {
      error.value = backendMessage
        ? `Сетевая ошибка: ${backendMessage}`
        : 'Сетевая ошибка при создании возможности'
    } else {
      error.value = backendMessage
        ? `Не удалось создать возможность: ${backendMessage}`
        : 'Не удалось создать возможность'
    }
  } finally {
    isGeocoding.value = false
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="opportunity-create container">
    <button type="button" class="opportunity-create__back-btn" @click="navigateTo('/')">
      <NuxtIcon name="material-symbols:arrow-back-rounded" size="16px" />
      Вернуться к выбору
    </button>

    <section class="opportunity-create__media bordered">
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        hidden
        @change="uploadMedia"
      />
      <div v-if="uploadedMedia.length" class="opportunity-create__media-carousel">
        <button
          type="button"
          class="opportunity-create__media-nav opportunity-create__media-nav--prev"
          @click="prevMediaSlide"
        >
          <NuxtIcon name="material-symbols:chevron-left-rounded" size="28px" />
        </button>
        <div class="opportunity-create__media-slide">
          <img
            :src="normalizeStorageAssetUrl(currentMediaItem?.url || currentMediaItem?.path || '')"
            alt=""
          />
          <button
            type="button"
            class="opportunity-create__media-remove"
            @click="removeMedia(currentMediaSlide)"
          >
            <NuxtIcon name="material-symbols:close-rounded" size="16px" />
          </button>
        </div>
        <button
          type="button"
          class="opportunity-create__media-nav opportunity-create__media-nav--next"
          @click="nextMediaSlide"
        >
          <NuxtIcon name="material-symbols:chevron-right-rounded" size="28px" />
        </button>
        <div v-if="mediaSlideCount > 1" class="opportunity-create__dots">
          <button
            v-for="(_, index) in uploadedMedia"
            :key="index"
            type="button"
            :class="[
              'opportunity-create__dot',
              { 'opportunity-create__dot--active': index === currentMediaSlide },
            ]"
            @click="goToMediaSlide(index)"
          />
        </div>
      </div>
      <img
        v-else
        src="/media/images/heroArt.webp"
        alt=""
        class="opportunity-create__media-image"
      />
      <button
        type="button"
        class="opportunity-create__media-add"
        :disabled="isUploadingMedia"
        @click="triggerMediaUpload"
      >
        <NuxtIcon name="material-symbols:add-photo-alternate-rounded" size="20px" />
        {{ isUploadingMedia ? 'Загрузка...' : 'Добавить' }}
      </button>
      <p v-if="mediaUploadError" class="opportunity-create__media-error">
        {{ mediaUploadError }}
      </p>
    </section>

    <section class="opportunity-create__title-row" :style="{ backgroundColor: titleRowBg }">
      <div class="opportunity-create__title-content">
        <div class="opportunity-create__title-head">
          <h1 class="opportunity-create__title">{{ displayTitle }}</h1>
          <button class="opportunity-create__edit-btn" type="button" @click="openEditTitle">
            <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
          </button>
        </div>
        <p v-if="displaySalary" class="opportunity-create__salary">{{ displaySalary }}</p>
      </div>
      <BaseAppButton
        type="button"
        variant="primary"
        class="opportunity-create__publish"
        :disabled="isSubmitting"
        @click="handlePublish"
      >
        {{
          isSubmitting ? (isGeocoding ? 'Определяем адрес...' : 'Публикация...') : 'Опубликовать'
        }}
      </BaseAppButton>
    </section>
    <p v-if="error" class="opportunity-create__error">{{ error }}</p>

    <section class="opportunity-create__about-row">
      <article class="opportunity-create__company bordered">
        <div class="opportunity-create__company-logo">
          <span>??</span>
        </div>
        <div class="opportunity-create__company-content">
          <p class="opportunity-create__company-title">{{ employer?.companyName || 'Компания' }}</p>
          <p class="opportunity-create__company-description">
            Информация о компании будет доступна после публикации
          </p>
        </div>
      </article>

      <article class="opportunity-create__tags bordered">
        <div class="opportunity-create__tags-head">
          <p class="opportunity-create__tags-title">Теги</p>
          <button class="opportunity-create__edit-btn" type="button" @click="openEditTags">
            <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
          </button>
        </div>
        <div v-if="editTags.length" class="opportunity-create__tags-list">
          <BaseAppTag v-for="tag in editTags" :key="tag.id" class="opportunity-create__tag">
            {{ tag.name }}
          </BaseAppTag>
        </div>
        <p v-else class="opportunity-create__tags-hint">Теги не выбраны</p>
      </article>
    </section>

    <section class="opportunity-create__details bordered">
      <h2 class="opportunity-create__details-title">Информация о возможности</h2>

      <article class="opportunity-create__description bordered">
        <div class="opportunity-create__description-head">
          <h3 class="opportunity-create__description-title">Краткое описание и требования</h3>
          <button class="opportunity-create__edit-btn" type="button" @click="openEditDescription">
            <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
          </button>
        </div>
        <p class="opportunity-create__description-text">{{ displayDescription }}</p>
      </article>

      <div class="opportunity-create__meta-grid">
        <article
          class="opportunity-create__meta-card opportunity-create__meta-card--format bordered"
        >
          <div class="opportunity-create__meta-head">
            <p class="opportunity-create__meta-label">Формат</p>
            <button class="opportunity-create__edit-btn" type="button" @click="openEditFormat">
              <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
            </button>
          </div>
          <p class="opportunity-create__meta-value">{{ displayFormatLabel }}</p>
        </article>

        <article class="opportunity-create__meta-card bordered">
          <div class="opportunity-create__meta-head">
            <p class="opportunity-create__meta-label">Место</p>
            <button class="opportunity-create__edit-btn" type="button" @click="openEditPlace">
              <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
            </button>
          </div>
          <p class="opportunity-create__meta-value">{{ displayPlace }}</p>
        </article>

        <article class="opportunity-create__meta-card bordered">
          <div class="opportunity-create__meta-head">
            <p class="opportunity-create__meta-label">Дата</p>
            <button class="opportunity-create__edit-btn" type="button" @click="openEditDate">
              <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
            </button>
          </div>
          <p class="opportunity-create__meta-value">{{ displayDate }}</p>
        </article>
      </div>
    </section>
  </div>

  <BaseAppModal
    :visible="activeModal === 'title'"
    title="Основная информация"
    @confirm="closeSectionModal"
    @cancel="closeSectionModal"
  >
    <FormInputField id="edit-title" label="Название" v-model="editTitle" />
    <label class="opportunity-create__modal-field">
      Тип возможности
      <select v-model="opportunityType" class="opportunity-create__modal-control bordered">
        <option value="VACANCY">Вакансия</option>
        <option value="INTERNSHIP">Стажировка</option>
        <option value="MENTORSHIP">Менторство</option>
        <option value="EVENT">Событие</option>
      </select>
    </label>
    <template v-if="showSalary">
      <FormInputField
        id="edit-salary-from"
        label="Зарплата от"
        type="number"
        v-model="editSalaryFrom"
      />
      <FormInputField
        id="edit-salary-to"
        label="Зарплата до"
        type="number"
        v-model="editSalaryTo"
      />
    </template>
  </BaseAppModal>

  <BaseAppModal
    :visible="activeModal === 'description'"
    title="Описание"
    @confirm="closeSectionModal"
    @cancel="closeSectionModal"
  >
    <label class="opportunity-create__modal-field">
      Описание и требования
      <textarea
        v-model="editDescription"
        class="opportunity-create__modal-textarea bordered"
        rows="5"
      />
    </label>
  </BaseAppModal>

  <BaseAppModal
    :visible="activeModal === 'tags'"
    title="Теги"
    @confirm="closeSectionModal"
    @cancel="closeSectionModal"
  >
    <BaseTagSelector v-model:selected-tags="editTags" :available-tags="availableTags" />
    <div v-if="editTags.length" class="opportunity-create__modal-tags-list">
      <BaseAppTag v-for="tag in editTags" :key="tag.id" class="opportunity-create__tag">
        {{ tag.name }}
      </BaseAppTag>
    </div>
  </BaseAppModal>

  <BaseAppModal
    :visible="activeModal === 'format'"
    title="Формат работы"
    @confirm="closeSectionModal"
    @cancel="closeSectionModal"
  >
    <label class="opportunity-create__modal-field">
      Формат
      <select v-model="editFormat" class="opportunity-create__modal-control bordered">
        <option value="REMOTE">Удалённо</option>
        <option value="OFFICE">Офис</option>
        <option value="HYBRID">Гибрид</option>
      </select>
    </label>
  </BaseAppModal>

  <BaseAppModal
    :visible="activeModal === 'place'"
    title="Место проведения"
    @confirm="closeSectionModal"
    @cancel="closeSectionModal"
  >
    <FormInputField id="edit-city" label="Город" v-model="editCity" />
    <FormInputField id="edit-address" label="Адрес" v-model="editAddress" />
  </BaseAppModal>

  <BaseAppModal
    :visible="activeModal === 'date'"
    title="Дата окончания"
    @confirm="closeSectionModal"
    @cancel="closeSectionModal"
  >
    <label class="opportunity-create__modal-field">
      Дата окончания публикации
      <input
        v-model="editExpiresAt"
        type="date"
        class="opportunity-create__modal-control bordered"
      />
    </label>
  </BaseAppModal>
</template>

<style lang="scss" scoped>
.opportunity-create {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
  margin-bottom: 24px;

  &__back-btn {
    border: 0;
    background-color: var(--primary-color);
    color: #fff;
    border-radius: 999px;
    padding: 8px 14px;
    width: fit-content;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-weight: 700;
    cursor: pointer;
  }

  &__media {
    position: relative;
    border-radius: 24px;
    overflow: hidden;
    height: 360px;
    background: #d8dde4;
  }

  &__media-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  &__media-carousel {
    position: relative;
    display: flex;
    align-items: center;
    height: 100%;
    width: 100%;
  }

  &__media-nav {
    position: absolute;
    z-index: 3;
    top: 50%;
    transform: translateY(-50%);
    border: none;
    background: rgba(0, 0, 0, 0.4);
    color: #fff;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;

    &--prev { left: 12px; }
    &--next { right: 12px; }
  }

  &__media-slide {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &__media-remove {
    position: absolute;
    top: 12px;
    right: 12px;
    background: rgba(0, 0, 0, 0.5);
    border: none;
    color: #fff;
    border-radius: 50%;
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 3;
  }

  &__dots {
    position: absolute;
    bottom: 16px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    gap: 8px;
  }

  &__dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, 0.6);
    background: transparent;
    cursor: pointer;
    padding: 0;

    &--active {
      background: #fff;
      border-color: #fff;
    }
  }

  &__media-add {
    position: absolute;
    bottom: 16px;
    right: 16px;
    background: rgba(0, 0, 0, 0.6);
    border: none;
    color: #fff;
    border-radius: 999px;
    padding: 8px 16px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    z-index: 2;

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }

  &__media-error {
    position: absolute;
    bottom: 60px;
    left: 16px;
    margin: 0;
    color: #e74c4c;
    font-size: 12px;
    font-weight: 600;
    background: rgba(0, 0, 0, 0.6);
    padding: 4px 10px;
    border-radius: 6px;
  }

  &__title-row {
    border-radius: 18px;
    color: #fff;
    padding: 16px 18px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  &__title-content {
    min-width: 0;
  }

  &__title-head {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__title {
    margin: 0;
    font-size: 40px;
    line-height: 1.1;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 800;
  }

  &__salary {
    margin: 4px 0 0;
    font-size: 16px;
    font-weight: 600;
    opacity: 0.9;
  }

  &__publish {
    background-color: #ff6f00 !important;
    color: #fff !important;
    border-radius: 999px;
    padding-inline: 18px;
    flex-shrink: 0;
  }

  &__error {
    margin: 0;
    color: #c74e4e;
    font-size: 13px;
    font-weight: 600;
  }

  &__about-row {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 300px;
    gap: 12px;
  }

  &__company {
    border-radius: 16px;
    padding: 14px;
    background-color: var(--background-secondary-color);
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__company-logo {
    width: 54px;
    height: 54px;
    border-radius: 50%;
    background-color: var(--primary-color);
    color: #fff;
    display: grid;
    place-items: center;
    font-weight: 800;
    overflow: hidden;
    flex-shrink: 0;
  }

  &__company-content {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__company-title {
    margin: 0;
    font-size: 20px;
    font-weight: 800;
    color: var(--text-inverted-color);
  }

  &__company-description {
    margin: 0;
    font-size: 12px;
    color: var(--text-tertiary-color);
    line-height: 1.3;
  }

  &__tags {
    border-radius: 16px;
    padding: 12px;
    background-color: var(--background-secondary-color);
  }

  &__tags-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 8px;
  }

  &__tags-title {
    margin: 0;
    font-size: 14px;
    font-weight: 800;
    color: var(--text-inverted-color);
  }

  &__tags-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  &__tag {
    font-size: 11px;
    padding-inline: 8px;
    padding-block: 4px;
  }

  &__tags-hint {
    margin: 0;
    font-size: 12px;
    color: var(--text-tertiary-color);
  }

  &__details {
    border-radius: 18px;
    padding: 12px;
    background-color: var(--background-secondary-color);
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  &__details-title {
    margin: 0;
    font-size: 36px;
    font-weight: 800;
    color: var(--text-inverted-color);
    font-family: 'Plus Jakarta Sans', sans-serif;
  }

  &__description {
    border-radius: 14px;
    padding: 12px;
    background-color: var(--background-primary-color);
  }

  &__description-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  &__description-title {
    margin: 0 0 8px;
    font-size: 13px;
    color: var(--text-inverted-color);
    font-weight: 800;
  }

  &__description-text {
    margin: 0;
    color: var(--text-inverted-color);
    font-size: 14px;
    line-height: 1.35;
  }

  &__meta-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 10px;
  }

  &__meta-card {
    border-radius: 14px;
    padding: 12px;
    background-color: var(--background-primary-color);
    min-height: 92px;

    &--format {
      background-color: var(--primary-color);

      .opportunity-create__meta-label,
      .opportunity-create__meta-value {
        color: #fff;
      }
    }
  }

  &__meta-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  &__meta-label {
    margin: 0 0 8px;
    font-size: 13px;
    font-weight: 700;
    color: var(--text-tertiary-color);
  }

  &__meta-value {
    margin: 0;
    font-size: 16px;
    font-weight: 700;
    color: var(--text-inverted-color);
    line-height: 1.3;
  }

  &__edit-btn {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    border: none;
    background: transparent;
    color: inherit;
    opacity: 0.6;
    cursor: pointer;
    display: grid;
    place-items: center;
    flex-shrink: 0;
    transition:
      opacity 0.2s ease,
      background-color 0.2s ease;

    &:hover {
      opacity: 1;
      background-color: rgba(255, 255, 255, 0.15);
    }
  }

  &__modal-field {
    display: flex;
    flex-direction: column;
    gap: 6px;
    font-size: 13px;
    font-weight: 700;
    color: var(--text-inverted-color);
  }

  &__modal-control {
    width: 100%;
    border-radius: 20px;
    padding: 10px 14px;
    background-color: var(--background-secondary-color);
    color: var(--text-inverted-color);
    border: 1px solid var(--border-color);
    font-size: 14px;
  }

  &__modal-textarea {
    width: 100%;
    border-radius: 20px;
    padding: 10px 14px;
    background-color: var(--background-secondary-color);
    color: var(--text-inverted-color);
    border: 1px solid var(--border-color);
    font-size: 14px;
    resize: vertical;
    min-height: 96px;
    font-family: inherit;
  }

  &__modal-tags-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-top: 8px;
  }
}

:global(.dark) .opportunity-create__description,
:global(.dark) .opportunity-create__meta-card,
:global(.dark) .opportunity-create__company,
:global(.dark) .opportunity-create__tags,
:global(.dark) .opportunity-create__details {
  background-color: #1f2633 !important;
}

:global(.light) .opportunity-create__company-title,
:global(.light) .opportunity-create__tags-title,
:global(.light) .opportunity-create__details-title,
:global(.light) .opportunity-create__description-title,
:global(.light) .opportunity-create__description-text,
:global(.light) .opportunity-create__meta-value {
  color: #1f2733 !important;
}

:global(.light) .opportunity-create__company-description,
:global(.light) .opportunity-create__meta-label {
  color: #5a6578 !important;
}

@media (max-width: 980px) {
  .opportunity-create {
    &__about-row {
      grid-template-columns: 1fr;
    }

    &__meta-grid {
      grid-template-columns: 1fr;
    }

    &__title-row {
      flex-direction: column;
      align-items: flex-start;
    }

    &__title {
      font-size: 30px;
    }
  }
}
</style>
