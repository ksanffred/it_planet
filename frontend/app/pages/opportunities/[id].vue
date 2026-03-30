<script lang="ts" setup>
import type { ApplicantResponsesLookup, OpportunityCardResponse } from '~/types'
import { formatOpporunityFormat } from '~/utils/formatOpportunityFormat'
import { normalizeStorageAssetUrl } from '~/utils/normalizeStorageAssetUrl'

const route = useRoute()
const config = useRuntimeConfig()
const tokenCookie = useCookie<string | null>('auth_token')

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
  if (!tokenCookie.value || !opportunity.value) {
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

watch([opportunity, tokenCookie, companyName], loadExistingResponse, { immediate: true })

const applyToOpportunity = async () => {
  if (!opportunity.value?.id || isApplyDisabled.value) {
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
</script>

<template>
  <div class="opportunity-page container">
    <button type="button" class="opportunity-page__back-btn" @click="navigateTo('/')">
      <NuxtIcon name="material-symbols:arrow-back-rounded" size="16px" />
      Вернуться к выбору
    </button>

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
        <h1 class="opportunity-page__title">{{ opportunity.title }}</h1>
        <BaseAppButton
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
            <p class="opportunity-page__company-description">{{ companyDescription }}</p>
            <p v-if="companyContacts" class="opportunity-page__company-contacts">
              {{ companyContacts }}
            </p>
          </div>
        </article>

        <article class="opportunity-page__tags bordered">
          <p class="opportunity-page__tags-title">Теги</p>
          <div class="opportunity-page__tags-list">
            <BaseAppTag v-for="tag in tagNames" :key="tag" class="opportunity-page__tag">
              {{ tag }}
            </BaseAppTag>
          </div>
        </article>
      </section>

      <section class="opportunity-page__details bordered">
        <h2 class="opportunity-page__details-title">Информация о возможности</h2>

        <article class="opportunity-page__description bordered">
          <h3 class="opportunity-page__description-title">Краткое описание и требования</h3>
          <p class="opportunity-page__description-text">
            {{ opportunity.description || 'Описание отсутствует' }}
          </p>
        </article>

        <div class="opportunity-page__meta-grid">
          <article class="opportunity-page__meta-card opportunity-page__meta-card--format bordered">
            <p class="opportunity-page__meta-label">Формат</p>
            <p class="opportunity-page__meta-value">{{ formatLabel }}</p>
          </article>

          <article class="opportunity-page__meta-card bordered">
            <p class="opportunity-page__meta-label">Место</p>
            <p class="opportunity-page__meta-value">{{ placeLabel }}</p>
          </article>

          <article class="opportunity-page__meta-card bordered">
            <p class="opportunity-page__meta-label">Дата</p>
            <p class="opportunity-page__meta-value">{{ dateLabel }}</p>
          </article>
        </div>
      </section>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.opportunity-page {
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

  &__tags-title {
    margin: 0 0 8px;
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

      .opportunity-page__meta-label,
      .opportunity-page__meta-value {
        color: #fff;
      }
    }
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
</style>
