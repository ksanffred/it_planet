<script lang="ts" setup>
import { normalizeStorageAssetUrl } from '~/utils/normalizeStorageAssetUrl'

type EmployerProfileById = {
  id: number
  userId: number
  companyName: string
  description?: string
  inn: string
  website?: string
  socials?: string
  logoUrl?: string
  verifiedOrgName?: string
  status: string
}

type EmployerOpportunityPosting = {
  id: number
  title: string
  status: 'ACTIVE' | 'CLOSED' | 'PLANNED' | string
  type: string
  published_at?: string
  expires_at?: string
  applications_count: number
}

type EmployerOpportunityApplicationItem = {
  applicant_id: number
  applicant_name: string
  university?: string
  desired_position?: string
  recommendation: number
  matching_tags?: Array<{
    id: number
    name: string
    category: string
  }>
}

const route = useRoute()
const config = useRuntimeConfig()
const tokenCookie = useCookie<string | null>('auth_token')

const employerId = computed(() => {
  const raw = Array.isArray(route.params.id) ? route.params.id[0] : route.params.id
  const parsed = Number(raw)
  return Number.isInteger(parsed) ? parsed : null
})

if (!tokenCookie.value) {
  navigateTo('/auth/login')
}

const authHeaders = {
  Authorization: `Bearer ${tokenCookie.value}`,
}

const opportunitiesSearch = ref('')
const responsesSearch = ref('')

const {
  data: employer,
  pending: employerPending,
  error: employerError,
} = await useFetch<EmployerProfileById>(`/employers/${employerId.value ?? ''}`, {
  baseURL: config.public.apiBase,
  method: 'GET',
  headers: authHeaders,
  immediate: Boolean(employerId.value),
})

const { data: opportunities } = await useFetch<EmployerOpportunityPosting[]>('/opportunities/me', {
  baseURL: config.public.apiBase,
  method: 'GET',
  headers: authHeaders,
  default: () => [],
})

const { data: responses } = await useFetch<EmployerOpportunityApplicationItem[]>(
  '/opportunities/responses/employer',
  {
    baseURL: config.public.apiBase,
    method: 'GET',
    headers: authHeaders,
    default: () => [],
  },
)

watchEffect(() => {
  if (employerError.value?.statusCode === 404) {
    navigateTo('/')
  }

  if (!employerPending.value && !employer.value) {
    navigateTo('/')
  }
})

const employerInitials = computed(() => {
  const source = employer.value?.companyName?.trim()
  if (!source) return '??'

  const parts = source.split(/\s+/).filter(Boolean)
  return parts
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('')
})

const employerLogoUrl = computed(() =>
  normalizeStorageAssetUrl(String(employer.value?.logoUrl ?? '')),
)

const profileDescription = computed(
  () => employer.value?.description || 'Описание компании не заполнено',
)
const profileLinks = computed(() => {
  const website = employer.value?.website
  const socials = employer.value?.socials
  return [website, socials].filter(Boolean).join(' • ') || 'Ссылки не указаны'
})

const filteredOpportunities = computed(() => {
  const query = opportunitiesSearch.value.trim().toLowerCase()
  if (!query) return opportunities.value ?? []

  return (opportunities.value ?? []).filter((item) =>
    `${item.title} ${item.type} ${item.status}`.toLowerCase().includes(query),
  )
})

const filteredResponses = computed(() => {
  const query = responsesSearch.value.trim().toLowerCase()
  if (!query) return responses.value ?? []

  return (responses.value ?? []).filter((item) =>
    `${item.applicant_name} ${item.desired_position} ${item.university}`
      .toLowerCase()
      .includes(query),
  )
})

const opportunityStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    ACTIVE: 'Активна',
    CLOSED: 'Закрыто',
    PLANNED: 'Запланировано',
  }
  return map[status] ?? status
}

const opportunityStatusClass = (status: string) => {
  const map: Record<string, string> = {
    ACTIVE: 'active',
    CLOSED: 'closed',
    PLANNED: 'planned',
  }
  return map[status] ?? 'default'
}

const opportunityStatusExtra = (item: EmployerOpportunityPosting) => {
  if (!item.expires_at) return ''
  const date = new Date(item.expires_at)
  if (Number.isNaN(date.getTime())) return ''
  return `до ${date.toLocaleDateString('ru-RU')}`
}
</script>

<template>
  <div class="employer-cabinet container">
    <header class="employer-cabinet__header">
      <h1 class="employer-cabinet__title">Личный кабинет</h1>
      <BaseBackButton />
    </header>

    <section class="employer-cabinet__profile bordered">
      <div class="employer-cabinet__profile-top">
        <div class="employer-cabinet__identity">
          <div class="employer-cabinet__avatar">
            <img
              v-if="employerLogoUrl"
              :src="employerLogoUrl"
              alt="Логотип компании"
              class="employer-cabinet__avatar-image"
            />
            <span v-else>{{ employerInitials }}</span>
          </div>
          <div class="employer-cabinet__identity-text">
            <p class="employer-cabinet__name">{{ employer?.companyName || 'Компания' }}</p>
            <p class="employer-cabinet__subtitle">Профиль компании</p>
          </div>
        </div>

        <BaseAppButton type="button" class="employer-cabinet__edit-btn" variant="primary">
          Редактировать
        </BaseAppButton>
      </div>

      <div class="employer-cabinet__profile-fields">
        <article class="employer-cabinet__profile-field bordered">
          <h3 class="employer-cabinet__profile-field-title">Описание</h3>
          <p class="employer-cabinet__profile-field-text">{{ profileDescription }}</p>
        </article>

        <article class="employer-cabinet__profile-field bordered">
          <h3 class="employer-cabinet__profile-field-title">Ссылки</h3>
          <p class="employer-cabinet__profile-field-text">{{ profileLinks }}</p>
        </article>

        <article class="employer-cabinet__profile-field bordered">
          <h3 class="employer-cabinet__profile-field-title">Адрес</h3>
          <p class="employer-cabinet__profile-field-text">Не указан</p>
        </article>
      </div>
    </section>

    <section class="employer-cabinet__columns">
      <article class="employer-cabinet__column bordered">
        <div class="employer-cabinet__column-head">
          <h2 class="employer-cabinet__section-title">Возможности</h2>
          <BaseAppButton type="button" variant="primary" class="employer-cabinet__new-btn">
            Новая возможность
          </BaseAppButton>
        </div>
        <BaseAppInput v-model="opportunitiesSearch" placeholder="Поиск по возможностям" />

        <div class="employer-cabinet__list">
          <div
            v-for="item in filteredOpportunities"
            :key="item.id"
            class="employer-cabinet__list-item bordered"
          >
            <div>
              <p class="employer-cabinet__item-title">{{ item.title }}</p>
              <p class="employer-cabinet__item-subtitle">{{ item.applications_count }} откликов</p>
            </div>
            <span
              :class="[
                'employer-cabinet__badge',
                `employer-cabinet__badge--${opportunityStatusClass(item.status)}`,
              ]"
            >
              {{ opportunityStatusLabel(item.status) }}
              <template v-if="opportunityStatusExtra(item)">
                {{ ` ${opportunityStatusExtra(item)}` }}
              </template>
            </span>
          </div>
          <p v-if="!filteredOpportunities.length" class="employer-cabinet__muted">
            Возможностей пока нет
          </p>
        </div>
      </article>

      <article class="employer-cabinet__column bordered">
        <h2 class="employer-cabinet__section-title">Отклики на возможности</h2>
        <p class="employer-cabinet__section-caption">
          Показываем отклики по выбранной возможности. Снимите выбор, чтобы увидеть всех кандидатов.
        </p>
        <BaseAppInput
          v-model="responsesSearch"
          placeholder="Поиск по откликам выбранной возможности"
        />

        <div class="employer-cabinet__list">
          <div
            v-for="item in filteredResponses"
            :key="`${item.applicant_id}-${item.applicant_name}`"
            class="employer-cabinet__list-item bordered"
          >
            <div>
              <p class="employer-cabinet__item-title">{{ item.applicant_name }}</p>
              <p class="employer-cabinet__item-subtitle">
                {{ item.desired_position || 'Позиция не указана' }} ·
                {{ item.recommendation }} рекомендаций
              </p>
            </div>
            <NuxtLink class="employer-cabinet__open-link" :to="`/applicants/${item.applicant_id}`">
              Открыть
            </NuxtLink>
          </div>
          <p v-if="!filteredResponses.length" class="employer-cabinet__muted">Откликов пока нет</p>
        </div>
      </article>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.employer-cabinet {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
  margin-bottom: 24px;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  &__title {
    margin: 0;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-size: 42px;
    line-height: 1;
    font-weight: 800;
    color: var(--text-inverted-color);
  }

  &__profile {
    border-radius: 18px;
    padding: 12px;
    background-color: var(--background-secondary-color);
  }

  &__profile-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    gap: 12px;
  }

  &__identity {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__avatar {
    width: 54px;
    height: 54px;
    border-radius: 50%;
    border: 1px solid #4f80ff;
    color: #2052d4;
    display: grid;
    place-items: center;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 800;
    font-size: 16px;
    background: #dce9fa;
    overflow: hidden;
    flex-shrink: 0;
  }

  &__avatar-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &__identity-text {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  &__name {
    margin: 0;
    font-size: 34px;
    line-height: 1;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 800;
    color: var(--text-inverted-color);
  }

  &__subtitle {
    margin: 0;
    font-size: 12px;
    color: var(--text-tertiary-color);
    font-weight: 500;
  }

  &__edit-btn {
    padding-inline: 14px;
    font-size: 12px;
    border-radius: 14px;
  }

  &__profile-fields {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__profile-field {
    background-color: var(--background-primary-color);
    border-radius: 10px;
    padding: 10px 12px;
  }

  &__profile-field-title {
    margin: 0 0 4px;
    font-size: 12px;
    line-height: 1.2;
    font-family: 'Plus Jakarta Sans', sans-serif;
    color: var(--text-inverted-color);
    font-weight: 800;
  }

  &__profile-field-text {
    margin: 0;
    font-size: 14px;
    color: var(--text-inverted-color);
  }

  &__columns {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  &__column {
    border-radius: 18px;
    padding: 12px;
    background-color: var(--background-secondary-color);
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  &__column-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  &__section-title {
    margin: 0;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-size: 34px;
    font-weight: 800;
    color: var(--text-inverted-color);
    line-height: 1.1;
  }

  &__section-caption {
    margin: 0;
    font-size: 13px;
    color: var(--text-tertiary-color);
    line-height: 1.25;
  }

  &__new-btn {
    border-radius: 999px;
    padding-inline: 14px;
    font-size: 12px;
    white-space: nowrap;
  }

  &__list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__list-item {
    border-radius: 10px;
    background-color: var(--background-primary-color);
    padding: 10px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
  }

  &__item-title {
    margin: 0;
    font-size: 16px;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 700;
    color: var(--text-inverted-color);
    line-height: 1.2;
  }

  &__item-subtitle {
    margin: 0;
    font-size: 14px;
    color: var(--text-tertiary-color);
    line-height: 1.2;
  }

  &__badge {
    padding: 6px 12px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 700;
    line-height: 1;
    color: var(--text-inverted-color);
    border: 1px solid var(--border-color);
    background-color: var(--background-primary-color);
    text-wrap: nowrap;

    &--active {
      background-color: #0b57c9;
      color: #fff;
      border-color: transparent;
    }

    &--planned {
      background-color: #ede6b5;
      color: #333;
      border-color: transparent;
    }

    &--closed {
      background-color: #f3ece1;
      color: #333;
      border-color: #c9bea8;
    }
  }

  &__open-link {
    border-radius: 999px;
    border: 1px solid var(--border-color);
    padding: 6px 14px;
    text-decoration: none;
    color: var(--text-inverted-color);
    font-size: 12px;
    font-weight: 700;
    white-space: nowrap;
  }

  &__muted {
    margin: 0;
    color: var(--text-tertiary-color);
    font-size: 13px;
  }
}

:global(.dark) .employer-cabinet__profile-field,
:global(.dark) .employer-cabinet__list-item {
  background-color: #1f2633 !important;
}

@media (max-width: 1100px) {
  .employer-cabinet {
    &__columns {
      grid-template-columns: 1fr;
    }
  }
}

@media (max-width: 768px) {
  .employer-cabinet {
    &__header {
      flex-direction: column;
      align-items: flex-start;
    }

    &__title {
      font-size: 30px;
    }

    &__profile-top {
      flex-direction: column;
      align-items: flex-start;
    }

    &__name {
      font-size: 24px;
    }

    &__section-title {
      font-size: 26px;
    }

    &__column-head {
      flex-direction: column;
      align-items: flex-start;
    }
  }
}
</style>
