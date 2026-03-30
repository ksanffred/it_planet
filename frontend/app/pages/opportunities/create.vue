<script lang="ts" setup>
import type {
  CreateOpportunityRequest,
  OpportunityFormat,
  OpportunityStatus,
  OpportunityType,
  Tag,
} from '~/types'

type EmployerMeResponse = {
  id: number
}

type CreatedOpportunityResponse = {
  id?: number
}

const config = useRuntimeConfig()
const tokenCookie = useCookie<string | null>('auth_token')

if (!tokenCookie.value) {
  await navigateTo('/auth/login?redirect=/opportunities/create')
}

const authHeaders = {
  Authorization: `Bearer ${tokenCookie.value}`,
}

const title = ref('')
const description = ref('')
const type = ref<OpportunityType>('VACANCY')
const format = ref<OpportunityFormat>('REMOTE')
const city = ref('')
const address = ref('')
const salaryFrom = ref<string>('')
const salaryTo = ref<string>('')
const expiresAt = ref('')
const status = ref<OpportunityStatus>('ACTIVE')
const selectedTags = ref<Tag[]>([])
const availableTags = ref<Tag[]>([])

const isSubmitting = ref(false)
const isGeocoding = ref(false)
const error = ref('')

const { data: employer, error: employerError } = await useFetch<EmployerMeResponse>(
  '/employers/me',
  {
    baseURL: config.public.apiBase,
    method: 'GET',
    headers: authHeaders,
  },
)

watchEffect(() => {
  if (employerError.value?.statusCode === 404) {
    navigateTo('/employers/register')
  } else if (employerError.value?.statusCode === 401) {
    navigateTo('/auth/login?redirect=/opportunities/create')
  }
})

const { data: tagsData } = await useFetch<Tag[]>('/tags', {
  baseURL: config.public.apiBase,
  method: 'GET',
  default: () => [],
})

watchEffect(() => {
  availableTags.value = tagsData.value ?? []
})

const toOptionalNumber = (value: string) => {
  const normalized = value.trim()
  if (!normalized) return undefined
  const parsed = Number(normalized)
  return Number.isFinite(parsed) ? parsed : undefined
}

const toIsoDate = (value: string) => {
  const normalized = value.trim()
  if (!normalized) return undefined
  return new Date(normalized).toISOString()
}

const resolveGeocodeApiKey = () =>
  String(config.public.yandexMapsApiKey || '071323d3-8c9a-48c3-9baf-5c437af0f80e').trim()

const geocodeAddress = async (query: string) => {
  const apiKey = resolveGeocodeApiKey()
  const response = await $fetch<{
    response?: {
      GeoObjectCollection?: {
        featureMember?: Array<{
          GeoObject?: {
            Point?: {
              pos?: string
            }
          }
        }>
      }
    }
  }>('https://geocode-maps.yandex.ru/1.x/', {
    method: 'GET',
    query: {
      apikey: apiKey,
      geocode: query,
      format: 'json',
      lang: 'ru_RU',
      results: 1,
    },
  })

  const pos =
    response.response?.GeoObjectCollection?.featureMember?.[0]?.GeoObject?.Point?.pos ?? ''
  const [lngRaw, latRaw] = String(pos).trim().split(/\s+/)
  const lat = Number(latRaw)
  const lng = Number(lngRaw)

  if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
    return null
  }

  return { lat, lng }
}

const handleCreateOpportunity = async () => {
  if (isSubmitting.value) return

  error.value = ''

  if (!title.value.trim()) {
    error.value = 'Укажите название возможности'
    return
  }

  if (!description.value.trim()) {
    error.value = 'Добавьте описание возможности'
    return
  }

  if (!city.value.trim() && !address.value.trim()) {
    error.value = 'Укажите город или адрес для определения координат'
    return
  }

  if (!employer.value?.id) {
    error.value = 'Не удалось определить организацию'
    return
  }

  isSubmitting.value = true
  isGeocoding.value = true

  try {
    const geocodeQuery = [city.value.trim(), address.value.trim()].filter(Boolean).join(', ')
    const coordinates = await geocodeAddress(geocodeQuery)
    if (!coordinates) {
      error.value = 'Не удалось определить координаты по указанному адресу'
      return
    }

    const payload: CreateOpportunityRequest = {
      employerId: employer.value.id,
      title: title.value.trim(),
      description: description.value.trim(),
      type: type.value,
      format: format.value,
      city: city.value.trim() || undefined,
      address: address.value.trim() || undefined,
      lat: coordinates.lat,
      lng: coordinates.lng,
      salaryFrom: toOptionalNumber(salaryFrom.value),
      salaryTo: toOptionalNumber(salaryTo.value),
      expiresAt: toIsoDate(expiresAt.value),
      status: status.value,
      tagIds: selectedTags.value.map((tag) => tag.id),
    }

    const response = await $fetch<CreatedOpportunityResponse>('/opportunities', {
      baseURL: config.public.apiBase,
      method: 'POST',
      headers: authHeaders,
      body: payload,
    })

    if (response?.id) {
      await navigateTo(`/opportunities/${response.id}`)
    } else {
      await navigateTo('/employers/me')
    }
  } catch (requestError) {
    const typedError = requestError as { statusCode?: number }
    if (typedError.statusCode === 400) {
      error.value = 'Проверьте корректность введенных данных'
    } else if (typedError.statusCode === 401) {
      error.value = 'Требуется авторизация'
    } else if (typedError.statusCode === 403) {
      error.value = 'Недостаточно прав для создания возможности'
    } else {
      error.value = 'Не удалось создать возможность'
    }
  } finally {
    isGeocoding.value = false
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="opportunity-create container">
    <AppForm
      class="opportunity-create__card"
      title="Создать возможность"
      description="Заполните карточку вакансии, стажировки, менторства или события"
    >
      <form class="opportunity-create__form" @submit.prevent="handleCreateOpportunity">
        <FormInputField
          id="title"
          v-model="title"
          label="Название"
          placeholder="Например, Junior Backend Intern"
          required
          type="text"
        />

        <label class="opportunity-create__field" for="description">
          Описание
          <textarea
            id="description"
            v-model="description"
            class="opportunity-create__textarea bordered"
            rows="4"
            placeholder="Кратко опишите требования, задачи и условия"
            required
          />
        </label>

        <label class="opportunity-create__field" for="type">
          Тип возможности
          <select id="type" v-model="type" class="opportunity-create__control bordered">
            <option value="VACANCY">Вакансия</option>
            <option value="INTERNSHIP">Стажировка</option>
            <option value="MENTORSHIP">Менторство</option>
            <option value="EVENT">Событие</option>
          </select>
        </label>

        <label class="opportunity-create__field" for="format">
          Формат
          <select id="format" v-model="format" class="opportunity-create__control bordered">
            <option value="REMOTE">Удаленно</option>
            <option value="OFFICE">Офис</option>
            <option value="HYBRID">Гибрид</option>
          </select>
        </label>

        <FormInputField id="city" v-model="city" label="Город" placeholder="Москва" type="text" />
        <FormInputField
          id="address"
          v-model="address"
          label="Адрес"
          placeholder="ул. Льва Толстого, 16"
          type="text"
        />

        <div class="opportunity-create__row">
          <label class="opportunity-create__field" for="salaryFrom">
            Зарплата от
            <input
              id="salaryFrom"
              v-model="salaryFrom"
              class="opportunity-create__control bordered"
              type="number"
              min="0"
              placeholder="50000"
            />
          </label>
          <label class="opportunity-create__field" for="salaryTo">
            Зарплата до
            <input
              id="salaryTo"
              v-model="salaryTo"
              class="opportunity-create__control bordered"
              type="number"
              min="0"
              placeholder="120000"
            />
          </label>
        </div>

        <label class="opportunity-create__field" for="expiresAt">
          Дата окончания публикации
          <input
            id="expiresAt"
            v-model="expiresAt"
            class="opportunity-create__control bordered"
            type="date"
          />
        </label>

        <label class="opportunity-create__field" for="status">
          Статус
          <select id="status" v-model="status" class="opportunity-create__control bordered">
            <option value="ACTIVE">Активна</option>
            <option value="PLANNED">Запланирована</option>
            <option value="CLOSED">Закрыта</option>
          </select>
        </label>

        <div class="opportunity-create__tags">
          <div class="opportunity-create__tags-top">
            <p class="opportunity-create__tags-title">Теги</p>
            <BaseTagSelector v-model:selected-tags="selectedTags" :available-tags="availableTags" />
          </div>
          <div v-if="selectedTags.length" class="opportunity-create__tags-list">
            <BaseAppTag v-for="tag in selectedTags" :key="tag.id" :bordered="true">
              {{ tag.name }}
            </BaseAppTag>
          </div>
          <p v-else class="opportunity-create__tags-hint">Выберите теги из списка</p>
        </div>

        <p v-if="error" class="opportunity-create__error">{{ error }}</p>

        <BaseAppButton type="submit" variant="primary" :disabled="isSubmitting">
          {{
            isSubmitting
              ? isGeocoding
                ? 'Определяем адрес...'
                : 'Создание...'
              : 'Создать возможность'
          }}
        </BaseAppButton>
      </form>
    </AppForm>
  </div>
</template>

<style lang="scss" scoped>
.opportunity-create {
  display: flex;
  align-items: center;
  justify-content: center;

  &__card {
    text-align: center;
    width: auto;
  }

  &__form {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  &__field {
    display: flex;
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
    font-size: 13px;
    color: var(--text-inverted-color);
    font-weight: 700;
  }

  &__textarea,
  &__control {
    width: 100%;
    border-radius: 20px;
    padding: 12px 16px;
    background-color: var(--background-secondary-color);
    color: var(--text-inverted-color);
  }

  &__textarea {
    resize: vertical;
    min-height: 96px;
  }

  &__row {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  &__tags {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  &__tags-top {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 10px;
  }

  &__tags-title {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
    color: var(--text-inverted-color);
  }

  &__tags-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  &__tags-hint {
    margin: 0;
    font-size: 12px;
    color: var(--text-tertiary-color);
  }

  &__error {
    margin: 0;
    font-size: 13px;
    color: var(--error-color);
  }
}

@media (max-width: 768px) {
  .opportunity-create {
    &__row {
      grid-template-columns: 1fr;
    }
  }
}
</style>
