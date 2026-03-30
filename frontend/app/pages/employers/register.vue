<script setup lang="ts">
import type { FetchError } from 'ofetch'
import type { RegisterEmployerRequest } from '~/types'

type DomainItem = {
  domains?: Array<{ domain?: string }>
}

const config = useRuntimeConfig()
const tokenCookie = useCookie<string | null>('auth_token')
const userCookie = useCookie<string | null>('user_data')

if (!tokenCookie.value) {
  navigateTo('/auth/login?redirect=/employers/register')
}

const companyName = ref('')
const description = ref('')
const inn = ref('')
const website = ref('')
const socials = ref('')
const logoUrl = ref('')
const error = ref('')
const isSubmitting = ref(false)

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

const parsedUserData = computed<{ id?: number; email?: string } | null>(() => {
  if (!userCookie.value) return null

  try {
    return JSON.parse(userCookie.value) as { id?: number; email?: string }
  } catch {
    return null
  }
})

const extractEmailDomain = (email: string) => {
  const atIndex = email.lastIndexOf('@')
  if (atIndex < 0) return ''
  return email
    .slice(atIndex + 1)
    .trim()
    .toLowerCase()
}

const validateEmailDomain = () => {
  const email = String(parsedUserData.value?.email ?? '')
    .trim()
    .toLowerCase()
  if (!email) {
    error.value = 'Не удалось определить email текущего аккаунта'
    return false
  }

  const domain = extractEmailDomain(email)
  if (!domain) {
    error.value = 'Некорректный email текущего аккаунта'
    return false
  }

  if (blockedDomains.value.has(domain)) {
    error.value = 'Этот email-домен запрещен для регистрации организации'
    return false
  }

  return true
}

const handleFormSubmit = async () => {
  if (isSubmitting.value) return

  error.value = ''

  if (!validateEmailDomain()) {
    return
  }

  const userId = Number(parsedUserData.value?.id)
  if (!Number.isInteger(userId)) {
    error.value = 'Не удалось определить пользователя. Войдите снова.'
    return
  }

  isSubmitting.value = true

  try {
    const payload: RegisterEmployerRequest = {
      userId,
      companyName: companyName.value.trim(),
      description: description.value.trim() || undefined,
      inn: inn.value.trim(),
      website: website.value.trim() || undefined,
      socials: socials.value.trim() || undefined,
      logoUrl: logoUrl.value.trim() || undefined,
    }

    await $fetch(`${config.public.apiBase}/employers/register`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${tokenCookie.value}`,
      },
      body: payload,
    })

    navigateTo('/auth/verify?mode=email-sent')
  } catch (requestError: unknown) {
    const err = requestError as FetchError

    if (err.statusCode === 409) {
      error.value = 'Профиль организации уже существует'
    } else if (err.statusCode === 400) {
      error.value = 'Проверьте корректность введенных данных'
    } else if (err.statusCode === 401) {
      error.value = 'Требуется авторизация'
    } else {
      error.value = 'Не удалось зарегистрировать организацию'
      console.error('Employer register error', requestError)
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <BaseBackButton class="back-button" />

  <AuthWrapper>
    <AppForm
      title="Регистрация организации"
      description="Заполните данные компании для публикации возможностей."
    >
      <form class="employer-register__form" @submit.prevent="handleFormSubmit">
        <FormInputField
          v-model="companyName"
          id="companyName"
          label="Название компании"
          placeholder="Acme Corp"
          required
          type="text"
        />
        <FormInputField
          v-model="description"
          id="description"
          label="Описание"
          placeholder="Global software company"
          required
          type="text"
        />
        <FormInputField
          v-model="inn"
          id="inn"
          label="ИНН"
          placeholder="7701234567"
          required
          type="text"
        />
        <FormInputField
          v-model="website"
          id="website"
          label="Сайт"
          placeholder="https://acme.com"
          required
          type="url"
        />
        <FormInputField
          v-model="socials"
          id="socials"
          label="Соцсети/контакты"
          placeholder="@acme_hr"
          required
          type="text"
        />
        <FormInputField
          v-model="logoUrl"
          id="logoUrl"
          label="Ссылка на логотип"
          placeholder="https://acme.com/logo.png"
          required
          type="url"
        />

        <p v-if="error" class="employer-register__error">{{ error }}</p>

        <BaseAppButton
          type="submit"
          variant="primary"
          :disabled="
            isSubmitting || !companyName || !description || !inn || !website || !socials || !logoUrl
          "
        >
          {{ isSubmitting ? 'Отправка...' : 'Зарегистрировать организацию' }}
        </BaseAppButton>
      </form>
    </AppForm>
  </AuthWrapper>
</template>

<style lang="scss" scoped>
.employer-register {
  &__form {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  &__error {
    margin: 0;
    color: var(--error-color);
    font-size: 13px;
    font-weight: 600;
  }
}

.back-button {
  top: 50px;
  left: 100px;
  position: absolute;
}
</style>
