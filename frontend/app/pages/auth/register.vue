<script setup lang="ts">
import type { UserRole } from '~/types'

const email: Ref<string> = ref('')
const description: Ref<string> = ref('')
const password: Ref<string> = ref('')
const name: Ref<string> = ref('')
const role: Ref<UserRole> = ref('APPLICANT')

const descriptions: Record<UserRole, string> = {
  USER: 'Начните поиск стажировок, вакансий, наставников и карьерных событий.',
  APPLICANT: 'Начните поиск стажировок, вакансий, наставников и карьерных событий.',
  EMPLOYER: 'Размещайте стажировки, вакансии, станьте наставником или разместите событие.',
  CURATOR: 'Станьте представителем своего ВУЗа и администратором платформы.',
}

definePageMeta({
  layout: false,
})
</script>

<template>
  <BaseBackButton class="back-button" />

  <AuthWrapper>
    <AppForm title="Регистрация " :description="descriptions[role]">
      <form class="auth__form">
        <label class="auth__label" for="name">
          {{ role === 'EMPLOYER' ? 'Название компании' : 'Отображаемое имя' }}
          <BaseAppInput
            class="auth__input"
            v-model="email"
            type="text"
            id="name"
            name="name"
            :placeholder="role == 'EMPLOYER' ? 'ООО Рога и Копыта' : 'Иван Иванов'"
            required
          />
        </label>
        <label v-if="role === 'EMPLOYER'" class="auth__label" for="description">
          Краткое описание компании
          <BaseAppInput
            class="auth__input"
            v-model="description"
            type="text"
            id="description"
            name="description"
            placeholder="Компания разработки ПО"
            required
          />
        </label>
        <label class="auth__label" for="email">
          {{ role === 'EMPLOYER' ? 'Корпоративный email' : 'Email' }}
          <BaseAppInput
            class="auth__input"
            v-model="email"
            type="email"
            id="email"
            name="email"
            autocomplete
            placeholder="you@example.com"
            required
          />
        </label>
        <label class="auth__label" for="password">
          Пароль
          <BaseAppInput
            class="auth__input"
            v-model="password"
            id="password"
            name="password"
            minlength="8"
            maxlength="2147483647"
            type="password"
            autocomplete
            placeholder="**********"
            required
          />
        </label>
        <label class="auth__label" for="role">
          Кто вы?
          <select class="auth__select" v-model="role" name="role" required id="role">
            <option value="APPLICANT" selected>Соискатель</option>
            <option value="EMPLOYER">Организация</option>
            <option value="CURATOR">Представитель ВУЗа</option>
          </select>
        </label>
        <BaseAppButton :disabled="!password || !email || !name" type="submit" variant="primary"
          >Зарегестрироваться</BaseAppButton
        >
      </form>
      <p class="auth__registration-link">
        Есть аккаунт?
        <NuxtLink to="/auth/login">Войти</NuxtLink>
      </p>
    </AppForm>
  </AuthWrapper>
</template>

<style lang="scss" scoped>
.auth {
  &__label {
    display: flex;
    flex-direction: column;
    gap: 8px;
    font-size: 13px;
    color: var(--text-inverted-color);

    font-weight: 700;
  }

  &__select {
    border-radius: 50vw;
    padding-block: 12px;
    border: 1px solid var(--border-color);
    padding-inline: 16px;
    background-color: var(--background-secondary-color);
    width: 100%;
    color: var(--text-inverted-color);
  }

  &__reset-link {
    text-decoration: none;
    color: var(--primary-color);
    font-weight: 700;
    font-size: 13px;
  }

  &__registration-link {
    color: var(--tertiary-color);
    font-weight: 700;
    font-size: 13px;

    & a {
      color: inherit;
      font-weight: inherit;
      font-size: inherit;
    }
  }

  &__form {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
}
.back-button {
  top: 50px;
  left: 100px;
  position: absolute;
}
</style>
