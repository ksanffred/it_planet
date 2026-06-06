<script lang="ts" setup>
import type {
  ApplicantResponsesLookup,
  OpportunityCardResponse,
  CreateOpportunityRequest,
  EmployerProfileResponse,
  TagResponse,
  OpportunityFormat,
} from '~/types'
import { formatOpporunityFormat } from '~/utils/formatOpportunityFormat'
import { normalizeStorageAssetUrl } from '~/utils/normalizeStorageAssetUrl'

const route = useRoute()
const config = useRuntimeConfig()
const tokenCookie = useCookie<string | null>('auth_token')
const userCookie = useCookie<string | null>('user_data')

const currentUserData = computed(() => {
  if (!userCookie.value) return null
  try {
    return JSON.parse(userCookie.value) as {
      id: number
      email: string
      role: string
    }
  } catch {
    return null
  }
})

const isEmployer = computed(() => currentUserData.value?.role === 'EMPLOYER')

const authHeaders = {
  Authorization: `Bearer ${tokenCookie.value}`,
}

const { data: currentEmployer } = isEmployer.value
  ? await useFetch<EmployerProfileResponse>('/employers/me', {
      baseURL: config.public.apiBase,
      method: 'GET',
      headers: authHeaders,
    })
  : { data: ref(null) }

const isOwnOpportunity = computed(
  () => isEmployer.value && opportunity.value?.employer?.id === currentEmployer.value?.id,
)

const opportunityId = computed(() => {
  const raw = Array.isArray(route.params.id) ? route.params.id[0] : route.params.id
  const parsed = Number(raw)
  return Number.isInteger(parsed) ? parsed : null
})

const {
  data: opportunity,
  pending,
  error,
} = await useFetch<OpportunityCardResponse>(`/opportunities/${opportunityId.value ?? ''}`, {
  baseURL: config.public.apiBase,
  method: 'GET',
  immediate: Boolean(opportunityId.value),
})

const currentSlide = ref(0)
const isApplying = ref(false)
const applyError = ref('')
const applySuccess = ref(false)
const hasExistingResponse = ref(false)

const mediaList = computed(() => {
  const list = (opportunity.value?.media ?? [])
    .map((item) => normalizeStorageAssetUrl(String(item ?? '').trim()))
    .filter((item) => item.length > 0 && item.toLowerCase() !== 'string')
  if (list.length > 0) {
    return list
  }
  return ['/media/images/heroArt.webp']
})

watch(mediaList, () => {
  currentSlide.value = 0
})

const slideCount = computed(() => mediaList.value.length)
const tagNames = computed(() => (opportunity.value?.tags ?? []).map((tag) => tag.name))

const employerData = computed(
  () => (opportunity.value?.employer ?? {}) as Record<string, string | number | undefined>,
)

const companyName = computed<string>(() =>
  String(employerData.value.companyName ?? employerData.value.name ?? 'Название компании'),
)
const companyDescription = computed<string>(() =>
  String(employerData.value.description ?? 'Информация о компании не указана'),
)
const companyContacts = computed(() => {
  const website = employerData.value.website
  const socials = employerData.value.socials
  const contacts = employerData.value.contacts
  return [website, socials, contacts].filter(Boolean).join(' • ')
})
const companyLogoUrl = computed(() =>
  normalizeStorageAssetUrl(String(employerData.value.logoUrl ?? '')),
)
const companyInitials = computed(() => {
  const source = companyName.value.trim()
  if (!source) return '?'
  return source
    .split(/\s+/)
    .slice(0, 2)
    .map((part: string) => part.charAt(0).toUpperCase())
    .join('')
})

const normalizeValue = (value: unknown) =>
  String(value ?? '')
    .trim()
    .toLowerCase()
const buildResponseSignature = (title: unknown, companyName: unknown, type: unknown) =>
  `${normalizeValue(title)}|${normalizeValue(companyName)}|${normalizeValue(type)}`

const formatLabel = computed(() =>
  opportunity.value ? formatOpporunityFormat(opportunity.value.format, 'ru') : 'Не указано',
)
const placeLabel = computed(() => {
  const city = opportunity.value?.city ?? ''
  const address = opportunity.value?.address ?? ''
  return [city, address].filter(Boolean).join(', ') || 'Не указано'
})
const dateLabel = computed(() => {
  if (!opportunity.value?.expiresAt && !opportunity.value?.publishedAt) {
    return 'Дата не указана'
  }

  const parts: string[] = []
  if (opportunity.value.publishedAt) {
    parts.push(
      `Опубликовано: ${new Date(opportunity.value.publishedAt).toLocaleDateString('ru-RU')}`,
    )
  }
  if (opportunity.value.expiresAt) {
    parts.push(`До: ${new Date(opportunity.value.expiresAt).toLocaleDateString('ru-RU')}`)
  }
  return parts.join(' · ')
})

const applyButtonLabel = computed(() => {
  if (hasExistingResponse.value) return 'Вы уже откликнулись'
  if (isApplying.value) return 'Отправка...'
  if (applySuccess.value) return 'Отклик отправлен'
  return 'Откликнуться'
})

const isApplyDisabled = computed(
  () => isApplying.value || applySuccess.value || hasExistingResponse.value,
)

const prevSlide = () => {
  if (slideCount.value <= 1) return
  currentSlide.value = (currentSlide.value - 1 + slideCount.value) % slideCount.value
}

const nextSlide = () => {
  if (slideCount.value <= 1) return
  currentSlide.value = (currentSlide.value + 1) % slideCount.value
}

const goToSlide = (index: number) => {
  if (index < 0 || index >= slideCount.value) return
  currentSlide.value = index
}

const loadExistingResponse = async () => {
  if (!tokenCookie.value || !opportunity.value || isEmployer.value) {
    hasExistingResponse.value = false
    return
  }

  try {
    const responses = await $fetch<ApplicantResponsesLookup[]>('/opportunities/responses/me', {
      baseURL: config.public.apiBase,
      method: 'GET',
      headers: {
        Authorization: `Bearer ${tokenCookie.value}`,
      },
    })

    const currentSignature = buildResponseSignature(
      opportunity.value.title,
      companyName.value,
      opportunity.value.type,
    )

    const signatures = new Set(
      (responses ?? []).map((item) =>
        buildResponseSignature(item.title, item.company_name, item.opportunity_type),
      ),
    )

    hasExistingResponse.value = signatures.has(currentSignature)
  } catch (requestError) {
    hasExistingResponse.value = false
    console.error('Failed to load current applicant responses', requestError)
  }
}

watch([opportunity, tokenCookie, companyName], loadExistingResponse, {
  immediate: true,
})

const applyToOpportunity = async () => {
  if (!opportunity.value?.id || isApplyDisabled.value || isEmployer.value) {
    return
  }

  if (!tokenCookie.value) {
    navigateTo('/auth/login')
    return
  }

  isApplying.value = true
  applyError.value = ''

  try {
    await $fetch(`/opportunities/${opportunity.value.id}/responses`, {
      baseURL: config.public.apiBase,
      method: 'POST',
      headers: {
        Authorization: `Bearer ${tokenCookie.value}`,
      },
    })
    applySuccess.value = true
    hasExistingResponse.value = true
  } catch (requestError) {
    const typedError = requestError as { statusCode?: number; status?: number }
    if (typedError.statusCode === 409 || typedError.status === 409) {
      applySuccess.value = true
      hasExistingResponse.value = true
      return
    }
    applyError.value = 'Не удалось отправить отклик'
    console.error('Failed to apply to opportunity', requestError)
  } finally {
    isApplying.value = false
  }
}

/* ── Редактирование возможности (работодатель) ── */
type EditSection = 'title' | 'description' | 'tags' | 'format' | 'place' | 'dates' | 'salary' | null

const activeModal = ref<EditSection>(null)
const isSaving = ref(false)
const saveError = ref('')

const editTitle = ref('')
const editDescription = ref('')
const editTags = ref<TagResponse[]>([])
const editFormat = ref<OpportunityFormat>('OFFICE')
const editCity = ref('')
const editAddress = ref('')
const editPublishedAt = ref('')
const editExpiresAt = ref('')
const editSalaryFrom = ref<number | null>(null)
const editSalaryTo = ref<number | null>(null)

const { data: availableTags } = await useFetch<TagResponse[]>('/tags', {
  baseURL: config.public.apiBase,
  method: 'GET',
  default: () => [],
})

const salaryLabel = computed(() => {
  const from = opportunity.value?.salaryFrom
  const to = opportunity.value?.salaryTo
  if (from != null && to != null) return `от ${from} до ${to}`
  if (from != null) return `от ${from}`
  if (to != null) return `до ${to}`
  return null
})

const toDateInputValue = (iso: string | undefined) => {
  if (!iso) return ''
  try {
    return new Date(iso).toISOString().split('T')[0] || ''
  } catch {
    return ''
  }
}

const toIsoValue = (date: string) => {
  if (!date) return null
  return `${date}T00:00:00`
}

const openEditTitle = () => {
  editTitle.value = opportunity.value?.title ?? ''
  activeModal.value = 'title'
}
const openEditDescription = () => {
  editDescription.value = opportunity.value?.description ?? ''
  activeModal.value = 'description'
}
const openEditTags = () => {
  editTags.value = [...(opportunity.value?.tags ?? [])]
  activeModal.value = 'tags'
}
const openEditFormat = () => {
  editFormat.value = opportunity.value?.format ?? 'OFFICE'
  activeModal.value = 'format'
}
const openEditPlace = () => {
  editCity.value = opportunity.value?.city ?? ''
  editAddress.value = opportunity.value?.address ?? ''
  activeModal.value = 'place'
}
const openEditDates = () => {
  editPublishedAt.value = toDateInputValue(opportunity.value?.publishedAt)
  editExpiresAt.value = toDateInputValue(opportunity.value?.expiresAt)
  activeModal.value = 'dates'
}
const openEditSalary = () => {
  editSalaryFrom.value = opportunity.value?.salaryFrom ?? null
  editSalaryTo.value = opportunity.value?.salaryTo ?? null
  activeModal.value = 'salary'
}

const closeModal = () => {
  activeModal.value = null
  saveError.value = ''
}

const saveSection = async () => {
  if (!opportunity.value || isSaving.value || !activeModal.value) return
  isSaving.value = true
  saveError.value = ''

  const body: Record<string, unknown> = {
    employerId: opportunity.value.employer.id,
    title: opportunity.value.title,
    description: opportunity.value.description ?? null,
    type: opportunity.value.type,
    format: opportunity.value.format,
    address: opportunity.value.address ?? null,
    city: opportunity.value.city ?? null,
    lat: opportunity.value.lat ?? null,
    lng: opportunity.value.lng ?? null,
    salaryFrom: opportunity.value.salaryFrom ?? null,
    salaryTo: opportunity.value.salaryTo ?? null,
    publishedAt: opportunity.value.publishedAt ?? null,
    expiresAt: opportunity.value.expiresAt ?? null,
    status: opportunity.value.status,
    media: opportunity.value.media,
    tagIds: (opportunity.value.tags ?? []).map((t) => t.id),
  }

  switch (activeModal.value) {
    case 'title':
      body.title = editTitle.value
      break
    case 'description':
      body.description = editDescription.value || null
      break
    case 'tags':
      body.tagIds = editTags.value.map((t) => t.id)
      break
    case 'format':
      body.format = editFormat.value
      break
    case 'place':
      body.city = editCity.value || null
      body.address = editAddress.value || null
      break
    case 'dates':
      body.publishedAt = toIsoValue(editPublishedAt.value)
      body.expiresAt = toIsoValue(editExpiresAt.value)
      break
    case 'salary':
      body.salaryFrom = editSalaryFrom.value ?? null
      body.salaryTo = editSalaryTo.value ?? null
      break
  }

  try {
    const updated = await $fetch<OpportunityCardResponse>(
      `/opportunities/${opportunity.value.id}`,
      {
        baseURL: config.public.apiBase,
        method: 'PUT',
        headers: authHeaders,
        body,
      },
    )
    opportunity.value = updated
    activeModal.value = null
  } catch (err) {
    saveError.value = 'Не удалось сохранить изменения'
    console.error('Failed to save opportunity', err)
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <div class="opportunity-page container">
    <BaseBackButton />

    <div v-if="pending" class="opportunity-page__state bordered">Загрузка...</div>
    <div v-else-if="error || !opportunity" class="opportunity-page__state bordered">
      Возможность не найдена
    </div>
    <template v-else>
      <section class="opportunity-page__media bordered">
        <button
          type="button"
          class="opportunity-page__nav opportunity-page__nav--prev"
          @click="prevSlide"
        >
          <NuxtIcon name="material-symbols:chevron-left-rounded" size="28px" />
        </button>
        <img
          :src="mediaList[currentSlide]"
          alt="Opportunity media"
          class="opportunity-page__media-image"
        />
        <button
          type="button"
          class="opportunity-page__nav opportunity-page__nav--next"
          @click="nextSlide"
        >
          <NuxtIcon name="material-symbols:chevron-right-rounded" size="28px" />
        </button>

        <div v-if="slideCount > 1" class="opportunity-page__dots">
          <button
            v-for="(_, index) in mediaList"
            :key="index"
            type="button"
            :class="[
              'opportunity-page__dot',
              { 'opportunity-page__dot--active': index === currentSlide },
            ]"
            @click="goToSlide(index)"
          />
        </div>
      </section>

      <section class="opportunity-page__title-row bordered">
        <div class="opportunity-page__title-wrap">
          <h1 class="opportunity-page__title">{{ opportunity.title }}</h1>
          <button
            v-if="isOwnOpportunity"
            type="button"
            class="opportunity-page__edit-btn"
            @click="openEditTitle"
            aria-label="Редактировать название"
          >
            <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
          </button>
        </div>
        <BaseAppButton
          v-if="!isEmployer"
          type="button"
          variant="primary"
          class="opportunity-page__apply"
          :disabled="isApplyDisabled"
          @click="applyToOpportunity"
        >
          {{ applyButtonLabel }}
        </BaseAppButton>
      </section>
      <p v-if="applyError" class="opportunity-page__error">{{ applyError }}</p>

      <section class="opportunity-page__about-row">
        <article class="opportunity-page__company bordered">
          <div class="opportunity-page__company-logo">
            <img
              v-if="companyLogoUrl"
              :src="companyLogoUrl"
              alt="Company logo"
              class="opportunity-page__company-logo-image"
            />
            <span v-else>{{ companyInitials }}</span>
          </div>
          <div class="opportunity-page__company-content">
            <p class="opportunity-page__company-title">{{ companyName }}</p>
            <p class="opportunity-page__company-description">
              {{ companyDescription }}
            </p>
            <p v-if="companyContacts" class="opportunity-page__company-contacts">
              {{ companyContacts }}
            </p>
          </div>
        </article>

        <article class="opportunity-page__tags bordered">
          <div class="opportunity-page__tags-head">
            <p class="opportunity-page__tags-title">Теги</p>
            <button
              v-if="isOwnOpportunity"
              type="button"
              class="opportunity-page__edit-btn"
              @click="openEditTags"
              aria-label="Редактировать теги"
            >
              <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
            </button>
          </div>
          <div class="opportunity-page__tags-list">
            <BaseAppTag
              v-for="tag in tagNames"
              bordered
              text-color="var(--text-primary-color)"
              :key="tag"
              class="opportunity-page__tag"
            >
              {{ tag }}
            </BaseAppTag>
          </div>
        </article>
      </section>

      <section class="opportunity-page__details bordered">
        <h2 class="opportunity-page__details-title">Информация о возможности</h2>

        <article class="opportunity-page__description bordered">
          <div class="opportunity-page__description-head">
            <h3 class="opportunity-page__description-title">Краткое описание и требования</h3>
            <button
              v-if="isOwnOpportunity"
              type="button"
              class="opportunity-page__edit-btn"
              @click="openEditDescription"
              aria-label="Редактировать описание"
            >
              <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
            </button>
          </div>
          <p class="opportunity-page__description-text">
            {{ opportunity.description || 'Описание отсутствует' }}
          </p>
        </article>

        <div class="opportunity-page__meta-grid">
          <article class="opportunity-page__meta-card opportunity-page__meta-card--format bordered">
            <div class="opportunity-page__meta-card-head">
              <p class="opportunity-page__meta-label">Формат</p>
              <button
                v-if="isOwnOpportunity"
                type="button"
                class="opportunity-page__edit-btn"
                @click="openEditFormat"
                aria-label="Редактировать формат"
              >
                <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
              </button>
            </div>
            <p class="opportunity-page__meta-value">{{ formatLabel }}</p>
          </article>

          <article class="opportunity-page__meta-card bordered">
            <div class="opportunity-page__meta-card-head">
              <p class="opportunity-page__meta-label">Место</p>
              <button
                v-if="isOwnOpportunity"
                type="button"
                class="opportunity-page__edit-btn"
                @click="openEditPlace"
                aria-label="Редактировать место"
              >
                <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
              </button>
            </div>
            <p class="opportunity-page__meta-value">{{ placeLabel }}</p>
          </article>

          <article class="opportunity-page__meta-card bordered">
            <div class="opportunity-page__meta-card-head">
              <p class="opportunity-page__meta-label">Дата</p>
              <button
                v-if="isOwnOpportunity"
                type="button"
                class="opportunity-page__edit-btn"
                @click="openEditDates"
                aria-label="Редактировать даты"
              >
                <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
              </button>
            </div>
            <p class="opportunity-page__meta-value">{{ dateLabel }}</p>
          </article>

          <article
            v-if="salaryLabel || isOwnOpportunity"
            class="opportunity-page__meta-card bordered"
          >
            <div class="opportunity-page__meta-card-head">
              <p class="opportunity-page__meta-label">Зарплата</p>
              <button
                v-if="isOwnOpportunity"
                type="button"
                class="opportunity-page__edit-btn"
                @click="openEditSalary"
                aria-label="Редактировать зарплату"
              >
                <NuxtIcon name="material-symbols:edit-rounded" size="16px" />
              </button>
            </div>
            <p class="opportunity-page__meta-value">
              {{ salaryLabel || 'Не указана' }}
            </p>
          </article>
        </div>
      </section>
    </template>
    <SvgRingShape class="ring-shape" />
    <SvgBlockShape class="block-shape" />
    <SvgBarShape class="bar-shape" />
  </div>

  <p v-if="saveError" class="opportunity-page__error opportunity-page__error--centered">
    {{ saveError }}
  </p>

  <BaseAppModal
    :visible="activeModal === 'title'"
    title="Название возможности"
    @confirm="saveSection"
    @cancel="closeModal"
  >
    <FormInputField id="edit-opp-title" label="Название" v-model="editTitle" />
  </BaseAppModal>

  <BaseAppModal
    :visible="activeModal === 'description'"
    title="Описание и требования"
    @confirm="saveSection"
    @cancel="closeModal"
  >
    <label class="opportunity-page__modal-field">
      Описание
      <textarea
        v-model="editDescription"
        class="opportunity-page__modal-textarea bordered"
        rows="6"
      />
    </label>
  </BaseAppModal>

  <BaseAppModal
    :visible="activeModal === 'tags'"
    title="Теги"
    @confirm="saveSection"
    @cancel="closeModal"
  >
    <BaseTagSelector
      :available-tags="availableTags"
      :selected-tags="editTags"
      @update:selected-tags="editTags = $event"
    />
    <div v-if="editTags.length" class="opportunity-page__edit-tags-preview">
      <BaseAppTag
        v-for="tag in editTags"
        :key="tag.id"
        text-color="var(--text-primary-color)"
        class="opportunity-page__tag"
      >
        {{ tag.name }}
      </BaseAppTag>
    </div>
  </BaseAppModal>

  <BaseAppModal
    :visible="activeModal === 'format'"
    title="Формат работы"
    @confirm="saveSection"
    @cancel="closeModal"
  >
    <div class="opportunity-page__format-tabs">
      <button
        type="button"
        :class="[
          'opportunity-page__format-tab',
          { 'opportunity-page__format-tab--active': editFormat === 'OFFICE' },
        ]"
        @click="editFormat = 'OFFICE'"
      >
        Офис
      </button>
      <button
        type="button"
        :class="[
          'opportunity-page__format-tab',
          { 'opportunity-page__format-tab--active': editFormat === 'HYBRID' },
        ]"
        @click="editFormat = 'HYBRID'"
      >
        Гибрид
      </button>
      <button
        type="button"
        :class="[
          'opportunity-page__format-tab',
          { 'opportunity-page__format-tab--active': editFormat === 'REMOTE' },
        ]"
        @click="editFormat = 'REMOTE'"
      >
        Удаленно
      </button>
    </div>
  </BaseAppModal>

  <BaseAppModal
    :visible="activeModal === 'place'"
    title="Место проведения"
    @confirm="saveSection"
    @cancel="closeModal"
  >
    <FormInputField id="edit-opp-city" label="Город" v-model="editCity" />
    <FormInputField id="edit-opp-address" label="Адрес" v-model="editAddress" />
  </BaseAppModal>

  <BaseAppModal
    :visible="activeModal === 'dates'"
    title="Даты"
    @confirm="saveSection"
    @cancel="closeModal"
  >
    <label class="opportunity-page__modal-field">
      Дата публикации
      <input type="date" v-model="editPublishedAt" class="opportunity-page__modal-date bordered" />
    </label>
    <label class="opportunity-page__modal-field">
      Дата окончания
      <input type="date" v-model="editExpiresAt" class="opportunity-page__modal-date bordered" />
    </label>
  </BaseAppModal>

  <BaseAppModal
    :visible="activeModal === 'salary'"
    title="Зарплата"
    @confirm="saveSection"
    @cancel="closeModal"
  >
    <FormInputField id="edit-opp-salary-from" label="От" type="number" v-model="editSalaryFrom" />
    <FormInputField id="edit-opp-salary-to" label="До" type="number" v-model="editSalaryTo" />
  </BaseAppModal>
</template>

<style lang="scss" scoped>
.opportunity-page {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
  margin-bottom: 24px;

  &__state {
    border-radius: 18px;
    padding: 16px;
    background-color: var(--background-secondary-color);
    font-weight: 700;
    color: var(--text-inverted-color);
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

  &__nav {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 44px;
    height: 44px;
    border-radius: 50%;
    border: 0;
    background: rgba(255, 255, 255, 0.9);
    color: #2f2f2f;
    display: grid;
    place-items: center;
    cursor: pointer;
    z-index: 1;

    &--prev {
      left: 12px;
    }

    &--next {
      right: 12px;
    }
  }

  &__dots {
    position: absolute;
    left: 50%;
    bottom: 12px;
    transform: translateX(-50%);
    display: inline-flex;
    gap: 8px;
  }

  &__dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    border: 0;
    background-color: rgba(255, 255, 255, 0.7);
    cursor: pointer;

    &--active {
      background-color: var(--primary-color);
    }
  }

  &__title-row {
    border-radius: 18px;
    background-color: #5c8f73;
    color: #fff;
    padding: 16px 18px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  &__title {
    margin: 0;
    font-size: 40px;
    line-height: 1.1;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 800;
  }

  &__apply {
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

  &__company-logo-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
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

  &__company-description,
  &__company-contacts {
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

  &__description-title {
    margin: 0;
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
  }

  &__error--centered {
    text-align: center;
    margin: 8px 0;
  }

  &__title-wrap {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
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

  &__description-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 8px;
  }

  &__meta-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 10px;
  }

  &__meta-card {
    border-radius: 14px;
    padding: 12px;
    background-color: var(--background-primary-color);
    min-height: 92px;

    &--format {
      background-color: var(--primary-color);

      .opportunity-page__meta-label,
      .opportunity-page__meta-value {
        color: #fff;
      }

      .opportunity-page__edit-btn {
        color: rgba(255, 255, 255, 0.7);

        &:hover {
          background-color: rgba(255, 255, 255, 0.15);
          color: #fff;
        }
      }
    }
  }

  &__meta-card-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 8px;
  }

  &__meta-label {
    margin: 0;
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

  &__modal-field {
    display: flex;
    flex-direction: column;
    gap: 6px;
    font-size: 13px;
    font-weight: 700;
    color: var(--text-inverted-color);
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
    min-height: 120px;
    font-family: inherit;
  }

  &__modal-date {
    width: 100%;
    border-radius: 20px;
    padding: 10px 14px;
    background-color: var(--background-secondary-color);
    color: var(--text-inverted-color);
    border: 1px solid var(--border-color);
    font-size: 14px;
    font-family: inherit;
  }

  &__format-tabs {
    display: flex;
    gap: 0;
    border: 1px solid var(--border-color);
    border-radius: 10px;
    overflow: hidden;
  }

  &__format-tab {
    flex: 1;
    padding: 8px 12px;
    border: none;
    background: transparent;
    color: var(--text-tertiary-color);
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 0.2s ease;

    &--active {
      background-color: var(--background-color);
      color: var(--text-inverted-color);
    }

    &:not(&--active):hover {
      background-color: var(--background-primary-color);
    }
  }

  &__edit-tags-preview {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }
}

:global(.dark) .opportunity-page__description,
:global(.dark) .opportunity-page__meta-card,
:global(.dark) .opportunity-page__company,
:global(.dark) .opportunity-page__tags,
:global(.dark) .opportunity-page__details,
:global(.dark) .opportunity-page__state {
  background-color: #1f2633 !important;
}

:global(.light) .opportunity-page__state,
:global(.light) .opportunity-page__company-title,
:global(.light) .opportunity-page__tags-title,
:global(.light) .opportunity-page__details-title,
:global(.light) .opportunity-page__description-title,
:global(.light) .opportunity-page__description-text,
:global(.light) .opportunity-page__meta-value {
  color: #1f2733 !important;
}

:global(.light) .opportunity-page__company-description,
:global(.light) .opportunity-page__company-contacts,
:global(.light) .opportunity-page__meta-label {
  color: #5a6578 !important;
}

@media (max-width: 980px) {
  .opportunity-page {
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

.ring-shape {
  position: absolute;
  z-index: -1;
  top: 100px;
  left: -100px;
  fill: var(--secondary-color);
}

.block-shape {
  position: absolute;
  z-index: -1;
  right: -100px;
  bottom: 200px;
  fill: var(--primary-color);
}

.bar-shape {
  position: absolute;
  z-index: -1;
  bottom: 150px;
  fill: var(--tertiary-color);
  left: -120px;
  transform: rotate(118deg);
}
</style>
