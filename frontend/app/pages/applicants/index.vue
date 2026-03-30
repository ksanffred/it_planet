<script lang="ts" setup>
import type { Tag } from '~/types'

const university = ref('')
const faculty = ref('')
const graduationYear = ref(0)
const portfolioUrl = ref('')
const additionalEducationDetails = ref('')
const currentFieldOfStudy = ref('')
const availableTags = ref<Tag[]>([])
const selectedTags = ref<Tag[]>([])

const userCookie = useCookie('user_data')
const tokenCookie = useCookie('auth_token')
const config = useRuntimeConfig()

if (!tokenCookie.value) {
  navigateTo('/auth/login')
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
        skillTagIds: selectedTags.value.map((tag) => tag.id),
      },
    })

    navigateTo('/applicants/me')
  } catch (error) {
    console.error(`File error: ${error}`)
  }
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
      <form @submit.prevent="handleCreateApplicant" class="applicant__form">
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
          label="Дополнительное образование"
          v-model="additionalEducationDetails"
          placeholder=""
          required
          type="text"
          id="additionalEducationDetails"
        />
        <FormInputField
          label="Год окончания"
          v-model="graduationYear"
          placeholder="Год когда вы закончили обучение"
          required
          type="number"
          min="1900"
          :max="2100"
          :value="1900"
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

          <div v-if="selectedTags.length > 0" class="applicant__tags-list">
            <BaseAppTag
              v-for="tag in selectedTags"
              :key="tag.id"
              :bordered="true"
              class="applicant__tag"
            >
              {{ tag.name }}
            </BaseAppTag>
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
    text-wrap: nowrap;
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
    flex-wrap: wrap;
    gap: 8px;
  }

  &__tag {
    cursor: default;
  }

  &__tags-hint {
    margin: 0;
    font-size: 12px;
    color: var(--text-tertiary-color);
  }
}
</style>
