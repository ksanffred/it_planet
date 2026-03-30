<script setup lang="ts">
import type { AuthResponse, UserRole } from '~/types'
import type { FetchError } from 'ofetch'

type DomainItem = {
  domains?: Array<{ domain?: string }>
}

const config = useRuntimeConfig()

const email: Ref<string> = ref('')
const password: Ref<string> = ref('')
const name: Ref<string> = ref('')
const role = ref<'APPLICANT' | 'EMPLOYER'>('APPLICANT')
const error: Ref<string> = ref('')

const { data: domainsData } = await useFetch<DomainItem[]>('/media/data/domains.json', {
  method: 'GET',
  default: () => [],
})

const blockedDomains = computed(() => {
  const set = new Set<string>()
  for (const item of domainsData.value ?? []) {
    for (const domainItem of item.domains ?? []) {
      const domain = String(domainItem.domain ?? '')
        .trim()
        .toLowerCase()
      if (domain) {
        set.add(domain)
      }
    }
  }
  return set
})

const extractEmailDomain = (emailValue: string) => {
  const atIndex = emailValue.lastIndexOf('@')
  if (atIndex < 0) return ''
  return emailValue
    .slice(atIndex + 1)
    .trim()
    .toLowerCase()
}

const handleFormSubmit = async () => {
  const normalizedEmail = email.value.trim().toLowerCase()
  if (role.value === 'EMPLOYER') {
    const domain = extractEmailDomain(normalizedEmail)
    if (domain && blockedDomains.value.has(domain)) {
      error.value = 'почта не корпоративная'
      return
    }
  }

  try {
    const response: AuthResponse = await $fetch(`${config.public.apiBase}/auth/register`, {
      method: 'POST',
      body: {
        email: normalizedEmail,
        displayName: name.value,
        password: password.value,
        role: role.value as UserRole,
      },
    })

    const authCookie = useCookie('auth_token', {
      maxAge: 60 * 60 * 24 * 7,
      path: '/',
      sameSite: 'lax',
    })
    authCookie.value = response.token

    const userCookie = useCookie('user_data', {
      maxAge: 60 * 60 * 24 * 7,
      path: '/',
      sameSite: 'lax',
    })
    userCookie.value = JSON.stringify({
      id: response.userId,
      email: response.email,
      displayName: response.displayName,
      role: response.role,
    })

    navigateTo('/applicants')
  } catch (registerError: unknown) {
    const err = registerError as FetchError

    if (err.statusCode === 409) {
      error.value = 'Пользователь с таким email уже существует'
      return
    }

    if (err.statusCode === 400) {
      error.value = 'Проверьте корректность введенных данных'
      return
    }

    error.value = 'Неизвестная ошибка'
    console.error(`Register error: ${registerError}`)
  }
}

definePageMeta({
  layout: false,
  middleware: 'guest-only',
})
</script>

<template>
  <BaseBackButton class="back-button" />

  <AuthWrapper>
    <AppForm
      title="Регистрация "
      description="Начните поиск стажировок, вакансий, наставников и карьерных событий."
    >
      <form class="auth__form" @submit.prevent="handleFormSubmit">
        <label class="auth__label" for="name">
          Отображаемое имя
          <BaseAppInput
            class="auth__input"
            v-model="name"
            type="text"
            id="name"
            name="name"
            placeholder="Иван Иванов"
            required
          />
        </label>
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
        <label class="auth__label" for="role">
          Роль
          <select id="role" v-model="role" class="auth__select bordered">
            <option value="APPLICANT">Соискатель</option>
            <option value="EMPLOYER">Работодатель</option>
          </select>
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
  &__error {
    color: var(--error-color);
  }

  &__label {
    display: flex;
    flex-direction: column;
    gap: 8px;
    font-size: 13px;
    color: var(--text-inverted-color);

    font-weight: 700;
  }

  &__select {
    width: 100%;
    border-radius: 50vw;
    padding: 12px 16px;
    background-color: var(--background-secondary-color);
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
