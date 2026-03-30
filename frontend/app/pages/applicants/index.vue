<script lang="ts" setup>
import type { AuthResponse } from '~/types'

const university = ref('')
const faculty = ref('')
const graduationYear = ref(0)
const portfolioUrl = ref('')
const additionalEducationDetails = ref('')
const currentFieldOfStudy = ref('')

const userCookie = useCookie('user_data')
const tokenCookie = useCookie('auth_token')
const config = useRuntimeConfig()

const { data, error } = await useFetch('/applicants/me', {
  baseURL: config.public.apiBase,
  method: 'GET',
  headers: {
    Authorization: `Bearer ${tokenCookie.value}`,
  },
  lazy: true,
})

if (error.value?.statusCode !== 404) {
  navigateTo('/applicants/me')
}

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
}
</style>
