<script lang="ts" setup>
import type { Tag } from '~/types'

const university = ref('')
const faculty = ref('')
const graduationYear = ref(0)
const portfolioUrl = ref('')
const additionalEducationDetails = ref('')
const currentFieldOfStudy = ref('')
const desiredPosition = ref('')

const graduationDate = computed({
  get: () => (graduationYear.value ? `${graduationYear.value}-01-01` : ''),
  set: (val: string) => {
    graduationYear.value = val ? parseInt(val.split('-')[0] ?? '', 10) : 0
  },
})
const availableTags = ref<Tag[]>([])
const selectedTags = ref<Tag[]>([])

const userCookie = useCookie('user_data')
const tokenCookie = useCookie('auth_token')
const config = useRuntimeConfig()

if (!tokenCookie.value) {
  navigateTo('/auth/login')
}

const userData = (() => {
  if (!userCookie.value) return null
  try { return JSON.parse(userCookie.value) } catch { return null }
})()

if (userData?.role === 'EMPLOYER') {
  navigateTo('/employers/me')
}

const { data, pending, error } = await useFetch('/applicants/me', {
  baseURL: config.public.apiBase,
  method: 'GET',
  headers: {
    Authorization: `Bearer ${tokenCookie.value}`,
  },
})

watchEffect(() => {
  if (pending.value) return

  if (data.value) {
    navigateTo('/applicants/me')
    return
  }

  if (error.value && error.value.statusCode !== 404) {
    navigateTo('/auth/login')
  }
})

const { data: tagsData } = await useFetch<Tag[]>('/tags', {
  baseURL: config.public.apiBase,
  method: 'GET',
  headers: {
    Authorization: `Bearer ${tokenCookie.value}`,
  },
  default: () => [],
})

watchEffect(() => {
  availableTags.value = tagsData.value ?? []
})

const tagsContainerRef = ref<HTMLElement | null>(null)
const formRef = ref<HTMLElement | null>(null)
const visibleTags = ref<Tag[]>([])
const hiddenTagsCount = ref(0)

const computeVisibleTags = () => {
  if (!tagsContainerRef.value) return

  const container = tagsContainerRef.value
  const formEl = container.closest('.applicant__form') as HTMLElement | null
  const containerWidth = formEl ? formEl.offsetWidth : container.offsetWidth
  const tagElements = container.querySelectorAll<HTMLDivElement>('.applicant__tag-item')

  if (!tagElements || tagElements.length === 0) {
    visibleTags.value = [...selectedTags.value]
    hiddenTagsCount.value = 0
    return
  }

  let totalWidth = 0
  let visibleCount = 0
  const gap = 8

  for (let i = 0; i < tagElements.length; i++) {
    const el = tagElements[i]
    if (!el) break
    totalWidth += el.offsetWidth + gap
    if (totalWidth <= containerWidth) {
      visibleCount++
    } else {
      totalWidth -= el.offsetWidth + gap
      break
    }
  }

  if (visibleCount >= selectedTags.value.length) {
    visibleTags.value = [...selectedTags.value]
    hiddenTagsCount.value = 0
  } else {
    const moreEl = container.querySelector<HTMLDivElement>('.applicant__tag-more')
    const moreWidth = moreEl ? moreEl.offsetWidth + gap : 110

    while (visibleCount > 0 && totalWidth + moreWidth > containerWidth) {
      visibleCount--
      totalWidth -= (tagElements[visibleCount]?.offsetWidth ?? 0) + gap
    }

    visibleTags.value = selectedTags.value.slice(0, visibleCount)
    hiddenTagsCount.value = selectedTags.value.length - visibleCount
  }
}

watch(
  selectedTags,
  () => {
    visibleTags.value = [...selectedTags.value]
    hiddenTagsCount.value = 0
    nextTick(() => computeVisibleTags())
  },
  { deep: true, immediate: true },
)

onMounted(() => window.addEventListener('resize', computeVisibleTags))
onUnmounted(() => window.removeEventListener('resize', computeVisibleTags))

onMounted(() => {
  nextTick(() => {
    if (formRef.value) {
      const w = formRef.value.offsetWidth
      formRef.value.style.maxWidth = `${w}px`
    }
  })
})

const handleCreateApplicant = async () => {
  try {
    if (!userCookie.value) {
      navigateTo('/auth/login')
      return
    }
    const userId = JSON.parse(userCookie.value).id

    const response = await $fetch(`/applicants`, {
      baseURL: config.public.apiBase,
      method: 'POST',
      headers: {
        Authorization: `Bearer ${tokenCookie.value}`,
      },
      body: {
        userId,
        university: university.value,
        faculty: faculty.value,
        graduationYear: graduationYear.value,
        portfolioUrl: portfolioUrl.value,
        additionalEducationDetails: additionalEducationDetails.value,
        currentFieldOfStudy: currentFieldOfStudy.value,
        desiredPosition: desiredPosition.value,
        skillTagIds: selectedTags.value.map((tag) => tag.id),
      },
    })

    navigateTo('/applicants/me')
  } catch (error) {
    console.error(`File error: ${error}`)
  }
}

const removeTag = (tag: Tag) => {
  selectedTags.value = selectedTags.value.filter((t) => t.id !== tag.id)
}

// const handleFileChange = async (event: Event) => {
//   try {
//     if (!userCookie.value) {
//       navigateTo("/auth/login");
//       return;
//     }

//     const file = (event.target as HTMLInputElement).files?.[0] || "{}";
//     const formData = new FormData();
//     formData.append("file", file);

//     const userId = JSON.parse(userCookie.value).id;

//     const response: { url: string; path: string } = await $fetch(
//       `/applicants/${userId}/resume`,
//       {
//         baseURL: config.public.apiBase,
//         method: "POST",
//         headers: {
//           Authorization: `Bearer ${tokenCookie.value}`,
//         },
//         body: formData,
//       },
//     );

//     console.log(response);
//   } catch (error) {
//     console.error(`File error: ${error}`);
//   }
// };
</script>
<template>
  <div class="applicant container">
    <AppForm
      class="applicant__card"
      title="Заполните профиль"
      description="Немного информации о вас"
    >
      <form ref="formRef" @submit.prevent="handleCreateApplicant" class="applicant__form">
        <FormInputField
          label="Университет"
          placeholder="Название вашего университета"
          required
          type="text"
          v-model="university"
          id="university"
        />
        <FormInputField
          label="Факультет"
          v-model="faculty"
          placeholder="Ваш факультет"
          required
          type="text"
          id="faculty"
        />
        <FormInputField
          label="Направление"
          v-model="currentFieldOfStudy"
          placeholder="Направление обучения"
          required
          type="text"
          id="currentFieldOfStudy"
        />
        <FormInputField
          label="Желаемая позиция"
          v-model="desiredPosition"
          placeholder="Junior Backend Developer"
          required
          type="text"
          id="desiredPosition"
        />
        <FormInputField
          label="Дополнительное образование"
          v-model="additionalEducationDetails"
          placeholder=""
          required
          type="text"
          id="additionalEducationDetails"
        />
        <FormInputField
          label="Год окончания"
          v-model="graduationDate"
          placeholder="Дата окончания обучения"
          required
          type="date"
          id="graduationYear"
        />
        <FormInputField
          label="Ссылка на ваше портфолио"
          v-model="portfolioUrl"
          placeholder="Ссылка на GitHub/GitLab/GitVerse"
          required
          type="url"
          id="portfolioUrl"
        />
        <div class="applicant__tags">
          <div class="applicant__tags-top">
            <p class="applicant__tags-title">Выберите теги навыков</p>
            <BaseTagSelector v-model:selected-tags="selectedTags" :available-tags="availableTags" />
          </div>

          <div v-if="selectedTags.length > 0" class="applicant__tags-list" ref="tagsContainerRef">
            <div
              v-for="tag in visibleTags"
              :key="tag.id"
              class="applicant__tag-item"
              @click="removeTag(tag)"
            >
              <BaseAppTag
                :bordered="true"
                class="applicant__tag"
                text-color="var(--text-primary-color)"
              >
                <span class="applicant__tag-text">{{ tag.name }}</span>
                <span class="applicant__tag-close-overlay">
                  <NuxtIcon name="material-symbols:close-rounded" size="14px" />
                </span>
              </BaseAppTag>
            </div>
            <div v-if="hiddenTagsCount > 0" class="applicant__tag-item applicant__tag-more">
              <BaseAppTag
                text-color="var(--text-primary-color)"
                :bordered="true"
                class="applicant__tag"
              >
                и ещё {{ hiddenTagsCount }}
              </BaseAppTag>
            </div>
          </div>
          <p v-else class="applicant__tags-hint">Можно выбрать несколько тегов</p>
        </div>
        <!-- <FormInputField
          @change="handleFileChange"
          label="Ваше резюме в PDF"
          required
          type="file"
          accept="application/pdf"
          id="resumeUrl"
        /> -->
        <BaseAppButton type="submit" variant="primary">Создать профиль</BaseAppButton>
      </form>
    </AppForm>
  </div>
</template>
<style lang="scss" scoped>
.applicant {
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

  &__tags {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    max-width: 100%;
  }

  &__tags-top {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
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
    overflow: hidden;
    flex-wrap: nowrap;
    gap: 8px;
    max-width: 100%;
  }

  &__tag-item {
    position: relative;
    flex-shrink: 0;
    cursor: pointer;
  }

  &__tag {
    position: relative;
    cursor: pointer;
  }

  &__tag-text {
    display: inline;
  }

  &__tag-close-overlay {
    position: absolute;
    inset: 0;
    border-radius: 50vw;
    background-color: var(--background-secondary-color);
    display: none;
    align-items: center;
    justify-content: center;
    color: var(--text-inverted-color);
  }

  &__tag-item:hover &__tag-text {
    visibility: hidden;
  }

  &__tag-item:hover &__tag-close-overlay {
    display: flex;
  }

  &__tag-more {
    flex-shrink: 0;
    cursor: default;
  }

  &__tags-hint {
    margin: 0;
    font-size: 12px;
    color: var(--text-tertiary-color);
  }
}
</style>
