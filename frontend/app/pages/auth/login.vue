<script setup lang="ts">
import type { AuthResponse } from '~/types'
import type { FetchError } from 'ofetch'

const config = useRuntimeConfig()

const email: Ref<string> = ref('')
const password: Ref<string> = ref('')
const error: Ref<string> = ref('')

async function handleFormSubmit() {
  try {
    const response: AuthResponse = await $fetch(`${config.public.apiBase}/auth/login`, {
      method: 'POST',
      body: {
        email: email.value,
        password: password.value,
      },
    })

    const cookie = useCookie('auth_token', {
      maxAge: 60 * 60 * 24 * 7,
      path: '/',
      sameSite: 'lax',
    })
    cookie.value = response.token

    navigateTo(`/applicants/${response.userId}`)
  } catch (authError: unknown) {
    email.value = ''
    password.value = ''
    const err = authError as FetchError
    if (err.statusCode && err.statusCode === 401) {
      error.value = 'Неверная почта или пароль'
    } else {
      error.value = 'Неизвестная ошибка'
      console.error(`Login error: ${authError}`)
    }
  }
}

definePageMeta({
  layout: false,
})
</script>

<template>
  <BaseBackButton class="back-button" />
  <AuthWrapper>
    <AuthCard
      title="Вход в аккаунт"
      description="Продолжайте поиск стажировок, вакансий, наставников и карьерных событий."
    >
      <form class="auth__form" @submit.prevent="handleFormSubmit">
        <label class="auth__label" for="email">
          Email
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
        <p class="auth__error">{{ error }}</p>
        <NuxtLink class="auth__reset-link" to="/">Забыли пароль?</NuxtLink>
        <BaseAppButton :disabled="!password || !email" type="submit" variant="primary"
          >Войти</BaseAppButton
        >
      </form>
      <p class="auth__registration-link">
        Нет аккаунта?
        <NuxtLink to="/auth/register">Зарегестрироваться</NuxtLink>
      </p>
    </AuthCard>
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
  &__error {
    color: var(--error-color);
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
