<script lang="ts" setup>
import type {
  Applicant,
  ApplicantVisibility,
  ApplicantContactListItem,
  CurrentUserResponse,
  FavoriteOpportunityResponse,
  OpportunityMiniCard,
  ApplicantResponsesLookup,
  Tag,
} from '~/types'
import { normalizeStorageAssetUrl } from '~/utils/normalizeStorageAssetUrl'

type FavoriteCard = Omit<FavoriteOpportunityResponse, 'id'> & { id?: number }
type FavoriteCardWithOpportunityId = FavoriteCard & { opportunityId?: number }
type ContactLookup = ApplicantContactListItem & Record<string, unknown>

type EmployerListItem = {
  id: number
  companyName: string
  inn: string
  status: string
  logoUrl?: string
}

const config = useRuntimeConfig()
const tokenCookie = useCookie<string | null>('auth_token')

if (!tokenCookie.value) {
  navigateTo('/auth/login')
}

const authHeaders = {
  Authorization: `Bearer ${tokenCookie.value}`,
}

const { data: currentUser } = await useFetch<CurrentUserResponse>('/auth/me', {
  baseURL: config.public.apiBase,
  method: 'GET',
  headers: authHeaders,
  immediate: Boolean(tokenCookie.value),
})

const isCurator = computed(() => currentUser.value?.role === 'CURATOR')

const favoritesSearch = ref('')
const responsesSearch = ref('')
const isVisibilityLoading = ref(false)
const visibilityError = ref('')
const avatarFileInput = ref<HTMLInputElement | null>(null)
const isAvatarUploading = ref(false)
const avatarUploadError = ref('')
const resumeFileInput = ref<HTMLInputElement | null>(null)
const isResumeUploading = ref(false)
const resumeUploadError = ref('')
const newCompaniesSearch = ref('')
const rawEmployers = ref<EmployerListItem[]>([])
const isFetchingEmployers = ref(false)

const fetchAllEmployers = async () => {
  isFetchingEmployers.value = true
  const results: EmployerListItem[] = []
  const batchSize = 10
  let consecutiveMisses = 0
  const MAX_MISSES = 5

  for (let start = 1; start <= 500 && consecutiveMisses < MAX_MISSES; start += batchSize) {
    const promises = Array.from({ length: batchSize }, (_, i) =>
      $fetch<EmployerListItem>(`/employers/${start + i}`, {
        baseURL: config.public.apiBase,
        headers: authHeaders,
      }).catch(() => null),
    )
    const batch = await Promise.all(promises)
    const hits = batch.filter((x): x is EmployerListItem => x !== null)
    if (hits.length === 0) {
      consecutiveMisses++
    } else {
      consecutiveMisses = 0
      results.push(...hits)
    }
  }
  rawEmployers.value = results
  isFetchingEmployers.value = false
}

/* ── Модальные окна редактирования ── */
type EditSection =
  | 'name'
  | 'university'
  | 'additionalEducation'
  | 'skills'
  | 'portfolio'
  | 'desiredPosition'
  | null

const activeModal = ref<EditSection>(null)

const editUniversity = reactive({
  university: '',
  faculty: '',
  currentFieldOfStudy: '',
  graduationYear: 0,
})
const editAdditionalEducation = ref('')
const editPortfolioUrl = ref('')
const editName = ref('')
const editDesiredPosition = ref('')
const editSkills = ref<Tag[]>([])
const availableTags = ref<Tag[]>([])
const isSavingSection = ref(false)
const sectionSaveError = ref('')

const normalizeApplicantProfile = (profile: Applicant): Applicant => ({
  ...profile,
  avatarUrl: normalizeStorageAssetUrl(profile.avatarUrl),
  resumeUrl: normalizeStorageAssetUrl(profile.resumeUrl),
})

const {
  data: applicant,
  pending: applicantPending,
  error: applicantError,
} = await useFetch<Applicant>('/applicants/me', {
  baseURL: config.public.apiBase,
  method: 'GET',
  headers: authHeaders,
  transform: (profile) => normalizeApplicantProfile(profile),
})

const { data: contacts } = await useFetch<ApplicantContactListItem[]>('/applicants/me/contacts', {
  baseURL: config.public.apiBase,
  method: 'GET',
  headers: authHeaders,
  default: () => [],
})

const { data: favorites } = await useFetch<FavoriteCard[]>(
  '/applicants/me/favorites/opportunities',
  {
    baseURL: config.public.apiBase,
    method: 'GET',
    headers: authHeaders,
    default: () => [],
  },
)
const { data: miniCards } = await useFetch<OpportunityMiniCard[]>('/opportunities/mini-cards', {
  baseURL: config.public.apiBase,
  method: 'GET',
  default: () => [],
})

const { data: responses } = await useFetch<ApplicantResponsesLookup[]>(
  '/opportunities/responses/me',
  {
    baseURL: config.public.apiBase,
    method: 'GET',
    headers: authHeaders,
    default: () => [],
  },
)

const { data: tagsData } = await useFetch<Tag[]>('/tags', {
  baseURL: config.public.apiBase,
  method: 'GET',
  headers: authHeaders,
  default: () => [],
})

watchEffect(() => {
  availableTags.value = tagsData.value ?? []
})

watchEffect(() => {
  if (applicantError.value?.statusCode === 404) {
    navigateTo('/applicants')
  }

  if (!applicantPending.value && !applicant.value) {
    navigateTo('/applicants')
  }
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

const newCompanies = computed(() => rawEmployers.value.filter((e) => e.status !== 'full_verified'))

const filteredNewCompanies = computed(() => {
  const q = newCompaniesSearch.value.trim().toLowerCase()
  if (!q) return newCompanies.value
  return newCompanies.value.filter(
    (e) => e.companyName.toLowerCase().includes(q) || e.inn.toLowerCase().includes(q),
  )
})

onMounted(() => {
  if (isCurator.value) fetchAllEmployers()
})

const approveCompany = async (item: EmployerListItem) => {
  try {
    const full = (await $fetch(`/employers/${item.id}`, {
      baseURL: config.public.apiBase,
      headers: authHeaders,
    })) as Record<string, unknown>
    await $fetch(`/employers/${item.id}`, {
      baseURL: config.public.apiBase,
      method: 'PUT',
      headers: authHeaders,
      body: {
        userId: full.userId,
        companyName: full.companyName,
        description: full.description ?? null,
        inn: full.inn,
        website: full.website ?? null,
        socials: full.socials ?? null,
        logoUrl: full.logoUrl ?? null,
        verifiedOrgName: full.verifiedOrgName ?? null,
        status: 'full_verified',
      },
    })
    rawEmployers.value = rawEmployers.value.filter((e) => e.id !== item.id)
  } catch (err) {
    console.error('Failed to approve company', err)
  }
}

const rejectCompany = async (item: EmployerListItem) => {
  try {
    const full = (await $fetch(`/employers/${item.id}`, {
      baseURL: config.public.apiBase,
      headers: authHeaders,
    })) as Record<string, unknown>
    await $fetch(`/employers/${item.id}`, {
      baseURL: config.public.apiBase,
      method: 'PUT',
      headers: authHeaders,
      body: {
        userId: full.userId,
        companyName: full.companyName,
        description: full.description ?? null,
        inn: full.inn,
        website: full.website ?? null,
        socials: full.socials ?? null,
        logoUrl: full.logoUrl ?? null,
        verifiedOrgName: full.verifiedOrgName ?? null,
        status: 'auto_rejected',
      },
    })
    rawEmployers.value = rawEmployers.value.filter((e) => e.id !== item.id)
  } catch (err) {
    console.error('Failed to reject company', err)
  }
}

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

const resolveContactApplicantId = (contact: ApplicantContactListItem) => {
  const typedContact = contact as ContactLookup
  const selfApplicantId = toNumberOrNull(applicant.value?.id)

  const directKeys = [
    'applicantId',
    'applicant_id',
    'contactApplicantId',
    'contact_applicant_id',
    'id',
    'profileId',
    'profile_id',
    'requesterApplicantId',
    'requester_applicant_id',
    'recipientApplicantId',
    'recipient_applicant_id',
    'userId',
    'user_id',
  ]

  const pairCandidates: number[] = []

  for (const key of directKeys) {
    const parsed = toNumberOrNull(typedContact[key])
    if (parsed) {
      pairCandidates.push(parsed)
    }
  }

  const nestedApplicant = typedContact.applicant
  if (nestedApplicant && typeof nestedApplicant === 'object') {
    const nestedId = toNumberOrNull((nestedApplicant as Record<string, unknown>).id)
    if (nestedId) {
      pairCandidates.push(nestedId)
    }
  }

  const uniqueCandidates = [...new Set(pairCandidates)]
  if (uniqueCandidates.length === 0) {
    return null
  }

  if (!selfApplicantId) {
    return uniqueCandidates[0] ?? null
  }

  return uniqueCandidates.find((id) => id !== selfApplicantId) ?? null
}

const goToContactProfile = (contact: ApplicantContactListItem) => {
  const contactApplicantId = resolveContactApplicantId(contact)
  if (!contactApplicantId) {
    return
  }

  navigateTo(`/applicants/${contactApplicantId}`)
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

const visibilityIsPublic = computed(() => applicant.value?.visibility === 'PUBLIC')

const toggleVisibility = async () => {
  if (!applicant.value || isVisibilityLoading.value) {
    return
  }

  const nextVisibility: ApplicantVisibility =
    applicant.value.visibility === 'PUBLIC' ? 'PRIVATE' : 'PUBLIC'

  isVisibilityLoading.value = true
  visibilityError.value = ''

  try {
    const updatedApplicant = await $fetch<Applicant>('/applicants/me/visibility', {
      baseURL: config.public.apiBase,
      method: 'PUT',
      headers: authHeaders,
      body: {
        visibility: nextVisibility,
      },
    })

    applicant.value = normalizeApplicantProfile(updatedApplicant)
  } catch (error) {
    visibilityError.value = 'Не удалось изменить видимость профиля'
    console.error('Failed to update profile visibility', error)
  } finally {
    isVisibilityLoading.value = false
  }
}

const openAvatarPicker = () => {
  if (isAvatarUploading.value) {
    return
  }

  avatarFileInput.value?.click()
}

const uploadAvatar = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]

  if (!file || !applicant.value?.id) {
    return
  }

  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    avatarUploadError.value = 'Можно загрузить только изображение'
    input.value = ''
    return
  }

  isAvatarUploading.value = true
  avatarUploadError.value = ''

  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await $fetch<{ path?: string; url?: string }>(
      `/applicants/${applicant.value.id}/avatar`,
      {
        baseURL: config.public.apiBase,
        method: 'POST',
        headers: authHeaders,
        body: formData,
      },
    )

    const nextAvatarUrl = normalizeStorageAssetUrl(response.url ?? response.path ?? '')
    if (nextAvatarUrl && applicant.value) {
      applicant.value = {
        ...applicant.value,
        avatarUrl: nextAvatarUrl,
      }
    }
  } catch (error) {
    avatarUploadError.value = 'Не удалось загрузить аватар'
    console.error('Failed to upload avatar', error)
  } finally {
    isAvatarUploading.value = false
    input.value = ''
  }
}

const openResumePicker = () => {
  if (isResumeUploading.value) {
    return
  }

  resumeFileInput.value?.click()
}

const uploadResume = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]

  if (!file || !applicant.value?.id) {
    return
  }

  const isPdf = file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf')
  if (!isPdf) {
    resumeUploadError.value = 'Можно загрузить только PDF файл'
    input.value = ''
    return
  }

  isResumeUploading.value = true
  resumeUploadError.value = ''

  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await $fetch<{ path?: string; url?: string }>(
      `/applicants/${applicant.value.id}/resume`,
      {
        baseURL: config.public.apiBase,
        method: 'POST',
        headers: authHeaders,
        body: formData,
      },
    )

    const nextResumeUrl = normalizeStorageAssetUrl(response.url ?? response.path ?? '')
    if (nextResumeUrl && applicant.value) {
      applicant.value = {
        ...applicant.value,
        resumeUrl: nextResumeUrl,
      }
    }
  } catch (error) {
    resumeUploadError.value = 'Не удалось загрузить резюме'
    console.error('Failed to upload resume', error)
  } finally {
    isResumeUploading.value = false
    input.value = ''
  }
}

/* ── Открытие модалок ── */
const openEditName = () => {
  editName.value = applicant.value?.name ?? ''
  activeModal.value = 'name'
}

const openEditUniversity = () => {
  if (!applicant.value) return
  editUniversity.university = applicant.value.university ?? ''
  editUniversity.faculty = applicant.value.faculty ?? ''
  editUniversity.currentFieldOfStudy = applicant.value.currentFieldOfStudy ?? ''
  editUniversity.graduationYear = applicant.value.graduationYear ?? 0
  activeModal.value = 'university'
}

const openEditAdditionalEducation = () => {
  editAdditionalEducation.value = applicant.value?.additionalEducationDetails ?? ''
  activeModal.value = 'additionalEducation'
}

const openEditSkills = () => {
  editSkills.value = [...(applicant.value?.skills ?? [])]
  activeModal.value = 'skills'
}

const openEditPortfolio = () => {
  editPortfolioUrl.value = applicant.value?.portfolioUrl ?? ''
  activeModal.value = 'portfolio'
}

const openEditDesiredPosition = () => {
  editDesiredPosition.value = applicant.value?.desiredPosition ?? ''
  activeModal.value = 'desiredPosition'
}

/* ── Сохранение ── */
const saveSection = async () => {
  if (!applicant.value || isSavingSection.value || !activeModal.value) return

  isSavingSection.value = true
  sectionSaveError.value = ''

  const body: Record<string, unknown> = {
    name: applicant.value.name,
    university: applicant.value.university ?? null,
    faculty: applicant.value.faculty ?? null,
    currentFieldOfStudy: applicant.value.currentFieldOfStudy ?? null,
    desiredPosition: applicant.value.desiredPosition ?? null,
    major: applicant.value.major ?? null,
    graduationYear: applicant.value.graduationYear ?? null,
    additionalEducationDetails: applicant.value.additionalEducationDetails ?? null,
    portfolioUrl: applicant.value.portfolioUrl ?? null,
    resumeUrl: applicant.value.resumeUrl ?? null,
    skillTagIds: (applicant.value.skills ?? []).map((s) => s.id),
  }

  switch (activeModal.value) {
    case 'name':
      body.name = editName.value || null
      break
    case 'university':
      body.university = editUniversity.university || null
      body.faculty = editUniversity.faculty || null
      body.currentFieldOfStudy = editUniversity.currentFieldOfStudy || null
      body.graduationYear = editUniversity.graduationYear
        ? Number(editUniversity.graduationYear)
        : null
      break
    case 'additionalEducation':
      body.additionalEducationDetails = editAdditionalEducation.value || null
      break
    case 'skills':
      body.skillTagIds = editSkills.value.map((t) => t.id)
      break
    case 'portfolio':
      body.portfolioUrl = editPortfolioUrl.value || null
      break
    case 'desiredPosition':
      body.desiredPosition = editDesiredPosition.value || null
      break
  }

  try {
    const updated = await $fetch<Applicant>('/applicants/me', {
      baseURL: config.public.apiBase,
      method: 'PUT',
      headers: authHeaders,
      body,
    })
    applicant.value = normalizeApplicantProfile(updated)
    activeModal.value = null
  } catch (err) {
    sectionSaveError.value = 'Не удалось сохранить изменения'
    console.error('Failed to save section', err)
  } finally {
    isSavingSection.value = false
  }
}

const closeModal = () => {
  activeModal.value = null
  sectionSaveError.value = ''
}

const showLogoutModal = ref(false)

const handleLogout = () => {
  tokenCookie.value = null
  const userCookie = useCookie<string | null>('user_data')
  userCookie.value = null
  navigateTo('/auth/login')
}
</script>

<template>
  <div class="user-account container">
    <header class="user-account__header">
      <h1 class="user-account__page-title">Личный кабинет</h1>
      <BaseBackButton />
    </header>

    <section class="user-account__profile bordered">
      <div class="user-account__profile-top">
        <div class="user-account__identity">
          <button
            type="button"
            class="user-account__avatar user-account__avatar-button"
            :disabled="isAvatarUploading"
            @click="openAvatarPicker"
          >
            <img
              v-if="applicant?.avatarUrl"
              :src="applicant.avatarUrl"
              alt="Аватар пользователя"
              class="user-account__avatar-image"
            />
            <span v-else>{{ applicantInitials }}</span>
          </button>
          <input
            ref="avatarFileInput"
            type="file"
            accept="image/*"
            class="user-account__avatar-input"
            @change="uploadAvatar"
          />
          <div class="user-account__identity-text">
            <div class="user-account__name-row">
              <p class="user-account__name">
                {{ applicant?.name || 'Пользователь' }}
              </p>
              <button
                class="user-account__edit-btn"
                type="button"
                @click="openEditName"
                aria-label="Редактировать имя"
              >
                <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
              </button>
            </div>
            <div class="user-account__subtitle-row">
              <p class="user-account__subtitle">
                {{ applicant?.desiredPosition || 'Профиль пользователя' }}
              </p>
              <button
                class="user-account__edit-btn"
                type="button"
                @click="openEditDesiredPosition"
                aria-label="Редактировать позицию"
              >
                <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
              </button>
            </div>
            <p v-if="isAvatarUploading" class="user-account__muted">Загружаем аватар...</p>
            <p v-if="avatarUploadError" class="user-account__error">
              {{ avatarUploadError }}
            </p>
          </div>
        </div>

        <div class="user-account__actions">
          <label class="user-account__visibility-toggle">
            <input
              class="user-account__visibility-input"
              type="checkbox"
              :checked="visibilityIsPublic"
              :disabled="isVisibilityLoading"
              @change="toggleVisibility"
            />
            <span class="user-account__visibility-slider" />
            <span class="user-account__visibility-label">
              {{ visibilityIsPublic ? 'Профиль виден' : 'Профиль скрыт' }}
            </span>
          </label>
          <p v-if="visibilityError" class="user-account__error">
            {{ visibilityError }}
          </p>
        </div>
      </div>

      <div class="user-account__profile-grid">
        <article class="user-account__panel bordered">
          <div class="user-account__panel-head">
            <h3 class="user-account__panel-title">Общая информация об университете</h3>
            <button
              class="user-account__edit-btn"
              type="button"
              @click="openEditUniversity"
              aria-label="Редактировать"
            >
              <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
            </button>
          </div>
          <p>Университет: {{ applicant?.university || 'Не указано' }}</p>
          <p>Факультет: {{ applicant?.faculty || 'Не указано' }}</p>
          <p>Направление: {{ applicant?.currentFieldOfStudy || 'Не указано' }}</p>
          <p>Год выпуска: {{ applicant?.graduationYear || 'Не указано' }}</p>
        </article>

        <article class="user-account__panel bordered">
          <div class="user-account__panel-head">
            <h3 class="user-account__panel-title">Ключевое дополнительное образование</h3>
            <button
              class="user-account__edit-btn"
              type="button"
              @click="openEditAdditionalEducation"
              aria-label="Редактировать"
            >
              <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
            </button>
          </div>
          <p>
            {{ applicant?.additionalEducationDetails || 'Дополнительное образование не заполнено' }}
          </p>
        </article>

        <article class="user-account__panel bordered">
          <div class="user-account__panel-head">
            <h3 class="user-account__panel-title">Навыки</h3>
            <button
              class="user-account__edit-btn"
              type="button"
              @click="openEditSkills"
              aria-label="Редактировать"
            >
              <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
            </button>
          </div>
          <div class="user-account__skills">
            <BaseAppTag
              v-for="skill in applicant?.skills || []"
              :key="skill.id"
              text-color="var(--text-primary-color)"
              class="user-account__skill-tag"
              >{{ skill.name }}</BaseAppTag
            >
            <span v-if="!applicant?.skills?.length" class="user-account__muted"
              >Навыки не указаны</span
            >
          </div>
        </article>
      </div>

      <div class="user-account__profile-grid user-account__profile-grid--bottom">
        <article class="user-account__panel bordered">
          <div class="user-account__panel-head">
            <h3 class="user-account__panel-title">Портфолио</h3>
            <button
              class="user-account__edit-btn"
              type="button"
              @click="openEditPortfolio"
              aria-label="Редактировать"
            >
              <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
            </button>
          </div>
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
          <div class="user-account__panel-head">
            <h3 class="user-account__panel-title">Резюме</h3>
            <button
              type="button"
              class="user-account__edit-btn"
              :disabled="isResumeUploading"
              @click="openResumePicker"
              aria-label="Загрузить резюме"
            >
              <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
            </button>
            <input
              ref="resumeFileInput"
              type="file"
              accept="application/pdf"
              class="user-account__resume-input"
              @change="uploadResume"
            />
          </div>
          <a
            v-if="applicant?.resumeUrl"
            :href="applicant.resumeUrl"
            target="_blank"
            rel="noopener noreferrer"
            >{{ applicant.resumeUrl }}</a
          >
          <span v-else class="user-account__muted">Резюме не загружено</span>
          <p v-if="resumeUploadError" class="user-account__error">
            {{ resumeUploadError }}
          </p>
        </article>
      </div>
    </section>

    <BaseAppModal
      :visible="activeModal === 'university'"
      title="Общая информация об университете"
      @confirm="saveSection"
      @cancel="closeModal"
    >
      <FormInputField
        id="edit-university"
        label="Университет"
        v-model="editUniversity.university"
      />
      <FormInputField id="edit-faculty" label="Факультет" v-model="editUniversity.faculty" />
      <FormInputField
        id="edit-field"
        label="Направление"
        v-model="editUniversity.currentFieldOfStudy"
      />
      <FormInputField
        id="edit-year"
        label="Год выпуска"
        type="number"
        v-model="editUniversity.graduationYear"
      />
    </BaseAppModal>

    <BaseAppModal
      :visible="activeModal === 'additionalEducation'"
      title="Ключевое дополнительное образование"
      @confirm="saveSection"
      @cancel="closeModal"
    >
      <FormInputField
        id="edit-edu"
        label="Дополнительное образование"
        v-model="editAdditionalEducation"
      />
    </BaseAppModal>

    <BaseAppModal
      :visible="activeModal === 'skills'"
      title="Навыки"
      @confirm="saveSection"
      @cancel="closeModal"
    >
      <BaseTagSelector
        :available-tags="availableTags"
        :selected-tags="editSkills"
        @update:selected-tags="editSkills = $event"
      />
      <div v-if="editSkills.length" class="user-account__edit-skills-preview">
        <BaseAppTag
          v-for="tag in editSkills"
          :key="tag.id"
          text-color="var(--text-primary-color)"
          class="user-account__skill-tag"
          >{{ tag.name }}</BaseAppTag
        >
      </div>
    </BaseAppModal>

    <BaseAppModal
      :visible="activeModal === 'portfolio'"
      title="Портфолио"
      @confirm="saveSection"
      @cancel="closeModal"
    >
      <FormInputField
        id="edit-portfolio"
        label="Ссылка на портфолио"
        type="url"
        v-model="editPortfolioUrl"
      />
    </BaseAppModal>

    <BaseAppModal
      :visible="activeModal === 'desiredPosition'"
      title="Желаемая позиция"
      @confirm="saveSection"
      @cancel="closeModal"
    >
      <FormInputField id="edit-desiredPosition" label="Позиция" v-model="editDesiredPosition" />
    </BaseAppModal>

    <BaseAppModal
      :visible="activeModal === 'name'"
      title="Имя пользователя"
      @confirm="saveSection"
      @cancel="closeModal"
    >
      <FormInputField id="edit-name" label="Имя" v-model="editName" />
    </BaseAppModal>

    <p v-if="sectionSaveError" class="user-account__error user-account__error--centered">
      {{ sectionSaveError }}
    </p>

    <section class="user-account__contacts bordered">
      <h2 class="user-account__section-title">Профессиональные контакты</h2>
      <div class="user-account__contacts-list">
        <article
          v-for="(contact, idx) in contacts"
          :key="idx"
          :class="[
            'user-account__contact-card bordered',
            {
              'user-account__contact-card--interactive': Boolean(
                resolveContactApplicantId(contact),
              ),
            },
          ]"
          @click="goToContactProfile(contact)"
        >
          <div class="user-account__contact-avatar">
            {{ (contact.name || '?').charAt(0).toUpperCase() }}
          </div>
          <div class="user-account__contact-info">
            <p class="user-account__contact-name">{{ contact.name }}</p>
            <p class="user-account__contact-role">
              {{ contact.desired_profession }}
            </p>
          </div>
        </article>
        <p v-if="!contacts?.length" class="user-account__muted">Контактов пока нет</p>
      </div>
    </section>

    <section class="user-account__columns">
      <article class="user-account__column bordered">
        <h2 class="user-account__section-title">Избранные возможности</h2>
        <BaseAppInput v-model="favoritesSearch" placeholder="Поиск по возможностям" />

        <div class="user-account__list">
          <div
            v-for="(item, idx) in filteredFavorites"
            :key="idx"
            :class="[
              'user-account__list-item bordered',
              {
                'user-account__list-item--interactive': isFavoriteItemClickable(item),
              },
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
        <template v-if="!isCurator">
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
        </template>

        <template v-else>
          <h2 class="user-account__section-title">Новые Компании</h2>
          <BaseAppInput v-model="newCompaniesSearch" placeholder="Поиск по компаниям" />

          <div class="user-account__list">
            <div
              v-for="item in filteredNewCompanies"
              :key="item.id"
              class="user-account__list-item bordered user-account__list-item--clickable"
              @click="navigateTo(`/employers/${item.id}`)"
            >
              <div>
                <p class="user-account__item-title">{{ item.companyName }}</p>
                <p class="user-account__item-subtitle">ИНН {{ item.inn }}</p>
              </div>
              <div class="user-account__company-actions">
                <button
                  class="user-account__action-btn user-account__action-btn--approve"
                  type="button"
                  title="Утвердить"
                  @click.stop="approveCompany(item)"
                >
                  <NuxtIcon name="material-symbols:check-rounded" size="18px" />
                </button>
                <button
                  class="user-account__action-btn user-account__action-btn--reject"
                  type="button"
                  title="Отклонить"
                  @click.stop="rejectCompany(item)"
                >
                  <NuxtIcon name="material-symbols:close-rounded" size="18px" />
                </button>
              </div>
            </div>
            <p
              v-if="!filteredNewCompanies.length && !isFetchingEmployers"
              class="user-account__muted"
            >
              Новых компаний нет
            </p>
            <p v-if="isFetchingEmployers" class="user-account__muted">Загрузка...</p>
          </div>
        </template>
      </article>
    </section>
    <div class="user-account__logout-row">
      <BaseAppButton variant="secondary" class="bordered" @click="showLogoutModal = true">
        Выйти из профиля
      </BaseAppButton>
    </div>
  </div>

  <BaseAppModal
    :visible="showLogoutModal"
    title="Выход из профиля"
    confirm-text="Выйти"
    @confirm="handleLogout"
    @cancel="showLogoutModal = false"
  >
    <p class="user-account__logout-confirm-text">Вы уверены, что хотите выйти?</p>
  </BaseAppModal>
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

  &__avatar-button {
    padding: 0;
    cursor: pointer;
    overflow: hidden;

    &:disabled {
      cursor: not-allowed;
      opacity: 0.7;
    }
  }

  &__avatar-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &__avatar-input {
    display: none;
  }

  &__identity-text {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  &__name-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__name {
    margin: 0;
    font-size: 34px;
    line-height: 1;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 800;
    color: var(--text-inverted-color);
  }

  &__subtitle-row {
    display: flex;
    align-items: center;
    gap: 6px;
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

  &__actions {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 6px;
  }

  &__visibility-button {
    padding-inline: 14px;
    font-size: 12px;
    border-radius: 14px;
  }

  &__error {
    margin: 0;
    color: #c74e4e;
    font-size: 12px;
    font-weight: 600;
  }

  &__visibility-toggle {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
  }

  &__visibility-input {
    position: absolute;
    opacity: 0;
    pointer-events: none;
  }

  &__visibility-slider {
    width: 40px;
    height: 22px;
    border-radius: 999px;
    background-color: #c7c7c7;
    border: 1px solid var(--border-color);
    position: relative;
    transition: background-color 0.2s ease;

    &::after {
      content: '';
      position: absolute;
      left: 2px;
      top: 2px;
      width: 16px;
      height: 16px;
      border-radius: 50%;
      background-color: #fff;
      transition: transform 0.2s ease;
    }
  }

  &__visibility-input:checked + &__visibility-slider {
    background-color: var(--primary-color);
  }

  &__visibility-input:checked + &__visibility-slider::after {
    transform: translateX(18px);
  }

  &__visibility-input:disabled + &__visibility-slider {
    opacity: 0.65;
  }

  &__visibility-label {
    margin: 0;
    font-size: 11px;
    color: var(--text-tertiary-color);
    font-weight: 600;
  }

  &__profile-grid {
    display: grid;
    grid-template-columns: 1.1fr 1fr 1.4fr;
    gap: 10px;

    &--bottom {
      grid-template-columns: repeat(2, 1fr);
      margin-top: 10px;
    }
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

  &__panel-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  &__edit-btn {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    border: none;
    background: transparent;
    color: var(--text-tertiary-color);
    cursor: pointer;
    display: grid;
    place-items: center;
    flex-shrink: 0;
    transition:
      background-color 0.2s ease,
      color 0.2s ease;

    &:hover {
      background-color: var(--background-tertiary-color);
      color: var(--primary-color);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  &__error--centered {
    text-align: center;
  }

  &__edit-skills-preview {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }

  &__resume-input {
    display: none;
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

  &__logout-row {
    display: flex;
    justify-content: flex-end;
    margin-top: 8px;
  }

  &__logout-confirm-text {
    margin: 0;
    font-size: 14px;
    color: var(--text-inverted-color);
  }

  &__contacts {
    border-radius: 12px;
    padding: 10px;
    background-color: var(--background-secondary-color);
  }

  &__section-title {
    margin: 0 0 8px;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-size: 24px;
    font-weight: 800;
    color: var(--text-inverted-color);
  }

  &__contacts-list {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 8px;
  }

  &__contact-card {
    display: flex;
    align-items: center;
    gap: 8px;
    border-radius: 10px;
    padding: 8px;
    background-color: var(--background-secondary-color);
  }

  &__contact-card--interactive {
    cursor: pointer;

    &:hover {
      .user-account__contact-name {
        text-decoration: underline;
      }
    }
  }

  &__contact-avatar {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    border: 1px solid #4f80ff;
    color: #2052d4;
    display: grid;
    place-items: center;
    font-size: 10px;
    font-weight: 800;
    flex-shrink: 0;
    background: #dce9fa;
  }

  &__contact-info {
    min-width: 0;
  }

  &__contact-name {
    margin: 0;
    font-size: 11px;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 700;
    color: var(--text-inverted-color);
    line-height: 1.2;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__contact-role {
    margin: 0;
    font-size: 10px;
    color: var(--text-tertiary-color);
    line-height: 1.2;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
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
    max-height: 400px;
    overflow-y: auto;
    padding-right: 4px;

    &::-webkit-scrollbar { width: 6px; }
    &::-webkit-scrollbar-track { background: transparent; }
    &::-webkit-scrollbar-thumb { background: var(--border-color); border-radius: 3px; }
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

  &__list-item--clickable {
    cursor: pointer;
  }

  &__company-actions {
    display: flex;
    gap: 6px;
    flex-shrink: 0;
  }

  &__action-btn {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    border: none;
    display: grid;
    place-items: center;
    cursor: pointer;
    transition: opacity 0.2s ease;

    &--approve {
      background-color: #4e8f62;
      color: #fff;
      &:hover {
        opacity: 0.85;
      }
    }

    &--reject {
      background-color: #c74e4e;
      color: #fff;
      &:hover {
        opacity: 0.85;
      }
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
:global(.dark) .user-account__list-item,
:global(.dark) .user-account__contact-card {
  background-color: #1f2633 !important;
}

@media (max-width: 1200px) {
  .user-account {
    &__profile-grid {
      grid-template-columns: 1fr 1fr;
    }

    &__contacts-list {
      grid-template-columns: repeat(2, minmax(0, 1fr));
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

    &__actions {
      align-items: flex-start;
      width: 100%;
    }

    &__name {
      font-size: 22px;
    }

    &__profile-grid {
      grid-template-columns: 1fr;
    }

    &__contacts-list {
      grid-template-columns: 1fr;
    }

    &__columns {
      grid-template-columns: 1fr;
    }
  }
}
</style>
