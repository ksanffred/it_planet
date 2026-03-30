<script lang="ts" setup>
import type { Applicant } from '~/types'

const config = useRuntimeConfig()
const tokenCookie = useCookie('auth_token')
const {
  data: applicant,
  error,
  pending,
} = await useFetch<Applicant>('/applicants/me', {
  baseURL: config.public.apiBase,
  method: 'GET',
  headers: {
    Authorization: `Bearer ${tokenCookie.value}`,
  },
  lazy: true,
})

if (error.value?.statusCode === 404) {
  navigateTo('/applicants')
}
</script>

<template>
  <div class="user-account container">
    <header class="user-account__header">
      <h1 class="user-account__title">Личный кабинет</h1>
      <BaseBackButton />
    </header>
    <main>
      <section class="user-account__main bordered">
        <div class="user-account__main-top">
          <BaseUserIdentity
            avatar="ДК"
            :name="applicant ? applicant.name : 'Загрузка...'"
            subtitle="Профиль пользователя"
            size="lg"
          />

          <BaseAppButton class="user-account__action-button" variant="primary">
            Изменить профиль
          </BaseAppButton>
        </div>
        <div class="user-account__main-info">
          <div class="user-account__middle-row">
            <div class="user-account__card bordered">
              <h3 class="user-account__card-title">Общая информация об учебном заведении</h3>
              <span class="user-account__field">
                Университет:
                {{ applicant ? applicant.university : 'Загрузка...' }}
              </span>
              <span class="user-account__field">
                Факультет:
                {{ applicant ? applicant.faculty : 'Загрузка...' }}
              </span>
              <span class="user-account__field">
                Направление:
                {{ applicant ? applicant.currentFieldOfStudy : 'Загрузка...' }}
              </span>
              <span class="user-account__field">
                Год выпуска:
                {{ applicant ? applicant.graduationYear : 'Загрузка...' }}
              </span>
            </div>
            <div class="user-account__card bordered">
              <h3 class="user-account__card-title">Дополнительное образование</h3>
              <span class="user-account__field">
                -
                {{ applicant ? applicant.additionalEducationDetails : 'Загрузка...' }}
              </span>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>
<style lang="scss" scoped>
.user-account {
  display: flex;
  margin-top: 26px;
  flex-direction: column;
  gap: 16px;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__title {
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-size: 30px;
    color: var(--text-inverted-color);
  }

  &__main {
    padding: 20px;
    border-radius: 24px;
    display: flex;
    flex-direction: column;
    gap: 12px;

    &-top {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
  }

  &__card {
    background-color: var(--background-secondary-color);
    border-radius: 16px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    flex: 1;
    padding: 14px 16px 14px 16px;

    &-title {
      font-family: 'Plus Jakarta Sans', sans-serif;
      font-size: 13px;
    }
  }

  &__middle-row {
    display: flex;
    gap: 12px;
    justify-content: space-around;
  }

  &__field {
    font-weight: 500;
    font-size: 14px;
  }
}
</style>
