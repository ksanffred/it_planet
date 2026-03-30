<script lang="ts" setup>
import type {
  Applicant,
  OpportunityMiniCard,
  ApplicantResponsesLookup,
  FavoriteOpportunityResponse,
} from '~/types'

type FavoriteCard = Omit<FavoriteOpportunityResponse, 'id'> & { id?: number }
type FavoriteCardWithOpportunityId = FavoriteCard & { opportunityId?: number }
type ContactLookup = {
  id?: number | string
  applicantId?: number | string
  requesterApplicantId?: number | string
  recipientApplicantId?: number | string
  name?: string
  status?: string
}

const config = useRuntimeConfig()
const route = useRoute()
const tokenCookie = useCookie<string | null>('auth_token')

const applicantId = computed(() => {
  const raw = Array.isArray(route.params.id) ? route.params.id[0] : route.params.id
  const parsed = Number(raw)
  return Number.isInteger(parsed) ? parsed : null
})

const authHeaders = computed(() =>
  tokenCookie.value
    ? {
        Authorization: `Bearer ${tokenCookie.value}`,
      }
    : undefined,
)

const favoritesSearch = ref('')
const responsesSearch = ref('')
const isAddingContact = ref(false)
const addContactError = ref('')
const contactRequestSent = ref(false)
const alreadyInContacts = ref(false)

const {
  data: applicant,
  pending: applicantPending,
  error: applicantError,
} = await useFetch<Applicant>(`/applicants/${applicantId.value ?? ''}`, {
  baseURL: config.public.apiBase,
  method: 'GET',
  headers: authHeaders.value,
  immediate: Boolean(applicantId.value),
})

const hasCheckedSelfRedirect = ref(false)

watchEffect(() => {
  if (
    hasCheckedSelfRedirect.value ||
    !tokenCookie.value ||
    applicantPending.value ||
    !applicant.value
  ) {
    return
  }

  hasCheckedSelfRedirect.value = true

  void (async () => {
    try {
      const currentApplicant = await $fetch<Applicant>('/applicants/me', {
        baseURL: config.public.apiBase,
        method: 'GET',
        headers: authHeaders.value,
      })

      if (
        currentApplicant.id === applicant.value?.id &&
        currentApplicant.userId === applicant.value?.userId
      ) {
        navigateTo('/applicants/me')
      }
    } catch (error) {
      console.error('Failed to fetch current applicant for self-check', error)
    }
  })()
})

const { data: favorites } = await useFetch<FavoriteCard[]>(
  `/applicants/${applicantId.value ?? ''}/favorites/opportunities`,
  {
    baseURL: config.public.apiBase,
    method: 'GET',
    headers: authHeaders.value,
    immediate: Boolean(applicantId.value),
    default: () => [],
  },
)
const { data: miniCards } = await useFetch<OpportunityMiniCard[]>('/opportunities/mini-cards', {
  baseURL: config.public.apiBase,
  method: 'GET',
  default: () => [],
})

const { data: responses } = await useFetch<ApplicantResponsesLookup[]>(
  `/applicants/${applicantId.value ?? ''}/responses`,
  {
    baseURL: config.public.apiBase,
    method: 'GET',
    headers: authHeaders.value,
    immediate: Boolean(applicantId.value),
    default: () => [],
  },
)

const applicantLoadErrorMessage = computed(() => {
  if (!applicantId.value) {
    return 'Некорректный id профиля'
  }

  if (applicantPending.value || !applicantError.value) {
    return ''
  }

  const status = applicantError.value.statusCode
  if (status === 404) {
    return 'Профиль пользователя не найден'
  }
  if (status === 403) {
    return 'Этот профиль недоступен'
  }
  return 'Не удалось загрузить профиль пользователя'
})

const applicantInitials = computed(() => {
  const source = applicant.value?.name?.trim()
  if (!source) return '??'

  const parts = source.split(/\s+/).filter(Boolean)
  return parts
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('')
})

const filteredFavorites = computed(() => {
  const query = favoritesSearch.value.trim().toLowerCase()
  if (!query) return favorites.value ?? []

  return (favorites.value ?? []).filter((item) =>
    `${item.title} ${item.company_name}`.toLowerCase().includes(query),
  )
})

const filteredResponses = computed(() => {
  const query = responsesSearch.value.trim().toLowerCase()
  if (!query) return responses.value ?? []

  return (responses.value ?? []).filter((item) =>
    `${item.title} ${item.company_name}`.toLowerCase().includes(query),
  )
})

const favoriteStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    ACTIVE: 'Активна',
    CLOSED: 'Закрыто',
    PLANNED: 'Запланировано',
  }
  return map[status] ?? status
}

const responseStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    NOT_REVIEWED: 'Не рассмотрен',
    REVIEWED: 'В резерве',
    ACCEPTED: 'Принят',
    REJECTED: 'Отклонен',
  }
  return map[status] ?? status
}

const responseStatusClass = (status: string) => {
  const map: Record<string, string> = {
    NOT_REVIEWED: 'neutral',
    REVIEWED: 'neutral',
    ACCEPTED: 'success',
    REJECTED: 'danger',
  }
  return map[status] ?? 'neutral'
}

const toNumberOrNull = (value: unknown) => {
  const parsed = Number(value)
  return Number.isInteger(parsed) ? parsed : null
}

const normalizeValue = (value: unknown) =>
  String(value ?? '')
    .trim()
    .toLowerCase()
const buildFavoriteSignature = (title: unknown, companyName: unknown, type: unknown) =>
  `${normalizeValue(title)}|${normalizeValue(companyName)}|${normalizeValue(type)}`

const miniCardIdBySignature = computed(() => {
  const map = new Map<string, number>()
  for (const card of miniCards.value ?? []) {
    map.set(buildFavoriteSignature(card.title, card.employerName, card.type), card.id)
  }
  return map
})

const resolveFavoriteOpportunityId = (item: FavoriteCard) => {
  const typedItem = item as FavoriteCardWithOpportunityId & {
    opportunity?: { id?: number | string }
  }
  const directId =
    toNumberOrNull(typedItem.id) ??
    toNumberOrNull(typedItem.opportunityId) ??
    toNumberOrNull(typedItem.opportunity?.id)

  if (directId) {
    return directId
  }

  return (
    miniCardIdBySignature.value.get(
      buildFavoriteSignature(typedItem.title, typedItem.company_name, typedItem.type),
    ) ?? null
  )
}

const goToFavoriteOpportunity = (item: FavoriteCard) => {
  const opportunityId = resolveFavoriteOpportunityId(item)
  if (!opportunityId) {
    return
  }

  navigateTo(`/opportunities/${opportunityId}`)
}

const isFavoriteItemClickable = (item: FavoriteCard) => Boolean(resolveFavoriteOpportunityId(item))

const contactButtonLabel = computed(() => {
  if (alreadyInContacts.value) return 'Уже в контактах'
  if (contactRequestSent.value) return 'Запрос отправлен'
  if (isAddingContact.value) return 'Отправка...'
  return 'Добавить в контакты'
})

const isContactButtonDisabled = computed(
  () => isAddingContact.value || contactRequestSent.value || alreadyInContacts.value,
)

const detectExistingContact = (payload: unknown) => {
  if (!Array.isArray(payload) || !applicantId.value) {
    alreadyInContacts.value = false
    return
  }

  const targetId = applicantId.value
  const targetName = applicant.value?.name?.trim().toLowerCase() ?? ''

  alreadyInContacts.value = payload.some((raw) => {
    if (!raw || typeof raw !== 'object') return false
    const contact = raw as ContactLookup

    const directIdMatch =
      toNumberOrNull(contact.applicantId) === targetId ||
      toNumberOrNull(contact.requesterApplicantId) === targetId ||
      toNumberOrNull(contact.recipientApplicantId) === targetId

    if (directIdMatch) return true

    const status = (contact.status ?? '').toString().toLowerCase()
    const normalizedName = (contact.name ?? '').trim().toLowerCase()
    return Boolean(
      targetName &&
      normalizedName &&
      normalizedName === targetName &&
      ['accepted', 'sent', 'received', 'pending'].includes(status),
    )
  })
}

const loadMyContacts = async () => {
  if (!tokenCookie.value || !applicantId.value) {
    alreadyInContacts.value = false
    return
  }

  try {
    const contacts = await $fetch<unknown>('/applicants/me/contacts', {
      baseURL: config.public.apiBase,
      method: 'GET',
      headers: authHeaders.value,
    })

    detectExistingContact(contacts)
  } catch (error) {
    alreadyInContacts.value = false
    console.error('Failed to load contacts for contact-check', error)
  }
}

watch([applicantId, applicant, tokenCookie], loadMyContacts, { immediate: true })

const addToContacts = async () => {
  if (
    !applicantId.value ||
    isAddingContact.value ||
    contactRequestSent.value ||
    alreadyInContacts.value
  ) {
    return
  }

  if (!tokenCookie.value) {
    navigateTo('/auth/login')
    return
  }

  isAddingContact.value = true
  addContactError.value = ''

  try {
    await $fetch(`/applicants/me/contacts/${applicantId.value}`, {
      baseURL: config.public.apiBase,
      method: 'POST',
      headers: authHeaders.value,
    })

    contactRequestSent.value = true
  } catch (error) {
    const typedError = error as { statusCode?: number; status?: number }
    if (typedError.statusCode === 409 || typedError.status === 409) {
      alreadyInContacts.value = true
      return
    }

    addContactError.value = 'Не удалось отправить запрос в контакты'
    console.error('Failed to add applicant to contacts', error)
  } finally {
    isAddingContact.value = false
  }
}
</script>

<template>
  <div class="user-account container">
    <header class="user-account__header">
      <h1 class="user-account__page-title">Профиль пользователя</h1>
      <BaseBackButton />
    </header>

    <section v-if="!applicantLoadErrorMessage" class="user-account__profile bordered">
      <div class="user-account__profile-top">
        <div class="user-account__identity">
          <div class="user-account__avatar">{{ applicantInitials }}</div>
          <div class="user-account__identity-text">
            <p class="user-account__name">{{ applicant?.name || 'Пользователь' }}</p>
            <p class="user-account__subtitle">Публичный профиль</p>
          </div>
        </div>

        <BaseAppButton
          class="user-account__contact-button"
          variant="primary"
          :disabled="isContactButtonDisabled"
          @click="addToContacts"
        >
          {{ contactButtonLabel }}
        </BaseAppButton>
      </div>
      <p v-if="addContactError" class="user-account__error">{{ addContactError }}</p>

      <div class="user-account__profile-grid">
        <article class="user-account__panel bordered">
          <h3 class="user-account__panel-title">Общая информация об университете</h3>
          <p>Университет: {{ applicant?.university || 'Не указано' }}</p>
          <p>Факультет: {{ applicant?.faculty || 'Не указано' }}</p>
          <p>Направление: {{ applicant?.currentFieldOfStudy || 'Не указано' }}</p>
          <p>Год выпуска: {{ applicant?.graduationYear || 'Не указано' }}</p>
        </article>

        <article class="user-account__panel bordered">
          <h3 class="user-account__panel-title">Ключевое дополнительное образование</h3>
          <p>
            {{ applicant?.additionalEducationDetails || 'Дополнительное образование не заполнено' }}
          </p>
        </article>

        <article class="user-account__panel bordered">
          <h3 class="user-account__panel-title">Навыки</h3>
          <div class="user-account__skills">
            <BaseAppTag
              v-for="skill in applicant?.skills || []"
              :key="skill.id"
              class="user-account__skill-tag"
              >{{ skill.name }}</BaseAppTag
            >
            <span v-if="!applicant?.skills?.length" class="user-account__muted"
              >Навыки не указаны</span
            >
          </div>
        </article>

        <article class="user-account__panel bordered">
          <h3 class="user-account__panel-title">Портфолио</h3>
          <a
            v-if="applicant?.portfolioUrl"
            :href="applicant.portfolioUrl"
            target="_blank"
            rel="noopener noreferrer"
            >{{ applicant.portfolioUrl }}</a
          >
          <span v-else class="user-account__muted">Ссылка отсутствует</span>
        </article>

        <article class="user-account__panel bordered">
          <h3 class="user-account__panel-title">Резюме</h3>
          <a
            v-if="applicant?.resumeUrl"
            :href="applicant.resumeUrl"
            target="_blank"
            rel="noopener noreferrer"
            >{{ applicant.resumeUrl }}</a
          >
          <span v-else class="user-account__muted">Резюме не загружено</span>
        </article>
      </div>
    </section>

    <section v-if="!applicantLoadErrorMessage" class="user-account__columns">
      <article class="user-account__column bordered">
        <h2 class="user-account__section-title">Избранные возможности</h2>
        <BaseAppInput v-model="favoritesSearch" placeholder="Поиск по возможностям" />

        <div class="user-account__list">
          <div
            v-for="(item, idx) in filteredFavorites"
            :key="idx"
            :class="[
              'user-account__list-item bordered',
              { 'user-account__list-item--interactive': isFavoriteItemClickable(item) },
            ]"
            @click="goToFavoriteOpportunity(item)"
          >
            <div class="user-account__item-link">
              <p class="user-account__item-title">{{ item.title }}</p>
              <p class="user-account__item-subtitle">{{ item.company_name }}</p>
            </div>
            <span class="user-account__badge">
              {{ favoriteStatusLabel(item.status) }}
            </span>
          </div>
          <p v-if="!filteredFavorites.length" class="user-account__muted">
            Избранных возможностей нет
          </p>
        </div>
      </article>

      <article class="user-account__column bordered">
        <h2 class="user-account__section-title">Отклики по возможностям</h2>
        <BaseAppInput
          v-model="responsesSearch"
          placeholder="Поиск по откликам выбранной возможности"
        />

        <div class="user-account__list">
          <div
            v-for="(item, idx) in filteredResponses"
            :key="idx"
            class="user-account__list-item bordered"
          >
            <div>
              <p class="user-account__item-title">{{ item.title }}</p>
              <p class="user-account__item-subtitle">{{ item.company_name }}</p>
            </div>
            <span
              :class="[
                'user-account__badge',
                `user-account__badge--${responseStatusClass(item.response_status)}`,
              ]"
            >
              {{ responseStatusLabel(item.response_status) }}
            </span>
          </div>
          <p v-if="!filteredResponses.length" class="user-account__muted">Откликов пока нет</p>
        </div>
      </article>
    </section>

    <section v-else class="user-account__error-card bordered">
      <p class="user-account__error-card-text">{{ applicantLoadErrorMessage }}</p>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.user-account {
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

  &__page-title {
    margin: 0;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-size: 32px;
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
    width: 42px;
    height: 42px;
    border-radius: 50%;
    border: 1px solid #4f80ff;
    color: #2052d4;
    display: grid;
    place-items: center;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 800;
    font-size: 14px;
    background: #dce9fa;
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

  &__contact-button {
    padding-inline: 14px;
    font-size: 12px;
    border-radius: 14px;
  }

  &__error {
    margin: 0 0 10px;
    color: #c74e4e;
    font-size: 12px;
    font-weight: 600;
  }

  &__error-card {
    border-radius: 16px;
    padding: 20px;
    background-color: var(--background-secondary-color);
  }

  &__error-card-text {
    margin: 0;
    font-size: 16px;
    font-weight: 700;
    color: var(--text-inverted-color);
  }

  &__profile-grid {
    display: grid;
    grid-template-columns: 1.1fr 1fr 1.4fr;
    gap: 10px;
  }

  &__panel {
    background-color: var(--background-primary-color);
    border-radius: 12px;
    padding: 10px 12px;

    p {
      margin: 0;
      font-size: 12px;
      color: var(--text-inverted-color);
      line-height: 1.35;
    }
  }

  &__panel-title {
    margin: 0 0 6px;
    font-size: 12px;
    line-height: 1.2;
    font-family: 'Plus Jakarta Sans', sans-serif;
    color: var(--text-inverted-color);
    font-weight: 800;
  }

  &__skills {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }

  &__skill-tag {
    padding-block: 4px;
    padding-inline: 8px;
    font-size: 10px;
  }

  &__muted {
    margin: 0;
    color: var(--text-tertiary-color);
    font-size: 12px;
  }

  &__section-title {
    margin: 0 0 8px;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-size: 24px;
    font-weight: 800;
    color: var(--text-inverted-color);
  }

  &__columns {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }

  &__column {
    border-radius: 12px;
    padding: 10px;
    background-color: var(--background-secondary-color);
    display: flex;
    flex-direction: column;
    gap: 10px;
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

  &__list-item--interactive {
    cursor: pointer;

    &:hover .user-account__item-title {
      text-decoration: underline;
    }
  }

  &__item-title {
    margin: 0;
    font-size: 13px;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 700;
    color: var(--text-inverted-color);
    line-height: 1.2;
  }

  &__item-subtitle {
    margin: 0;
    font-size: 11px;
    color: var(--text-tertiary-color);
    line-height: 1.2;
  }

  &__item-link {
    min-width: 0;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }

  &__badge {
    padding: 6px 12px;
    border-radius: 999px;
    font-size: 11px;
    font-weight: 700;
    line-height: 1;
    color: var(--text-inverted-color);
    border: 1px solid var(--border-color);
    background-color: var(--background-primary-color);
    text-wrap: nowrap;
    text-transform: capitalize;

    &--success {
      background-color: #4e8f62;
      color: #fff;
      border-color: transparent;
    }

    &--danger {
      background-color: #f3ece1;
      color: #333;
    }

    &--neutral {
      background-color: #f3ece1;
      color: #333;
    }
  }
}

:global(.dark) .user-account__panel,
:global(.dark) .user-account__list-item {
  background-color: #1f2633 !important;
}

@media (max-width: 1200px) {
  .user-account {
    &__profile-grid {
      grid-template-columns: 1fr 1fr;
    }
  }
}

@media (max-width: 768px) {
  .user-account {
    &__header {
      flex-direction: column;
      align-items: flex-start;
    }

    &__page-title {
      font-size: 26px;
    }

    &__profile-top {
      flex-direction: column;
      align-items: flex-start;
    }

    &__name {
      font-size: 22px;
    }

    &__profile-grid {
      grid-template-columns: 1fr;
    }

    &__columns {
      grid-template-columns: 1fr;
    }
  }
}
</style>
