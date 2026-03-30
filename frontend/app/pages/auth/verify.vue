<script setup lang="ts">
const route = useRoute()
const config = useRuntimeConfig()

const token = computed(() => {
  const value = route.query.token
  return typeof value === 'string' ? value.trim() : ''
})
const mode = computed(() => {
  const value = route.query.mode
  return typeof value === 'string' ? value : ''
})

const countdown = ref(5)
const verificationStatus = ref<'idle' | 'success' | 'error'>('idle')
const verificationErrorMessage = ref('')

let timerId: ReturnType<typeof setInterval> | null = null

const startRedirectCountdown = () => {
  if (timerId) {
    clearInterval(timerId)
  }

  timerId = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      if (timerId) {
        clearInterval(timerId)
      }
      navigateTo('/')
    }
  }, 1000)
}

if (token.value) {
  try {
    await $fetch(`${config.public.apiBase}/auth/verify`, {
      method: 'GET',
      query: {
        token: token.value,
      },
    })

    verificationStatus.value = 'success'
    startRedirectCountdown()
  } catch (verifyError) {
    verificationStatus.value = 'error'
    verificationErrorMessage.value = 'Ссылка верификации недействительна или устарела'
    startRedirectCountdown()
    console.error('Verify token error', verifyError)
  }
} else if (mode.value === 'email-sent') {
  verificationStatus.value = 'idle'
} else {
  navigateTo('/')
}

onUnmounted(() => {
  if (timerId) {
    clearInterval(timerId)
  }
})

definePageMeta({
  layout: false,
})
</script>

<template>
  <BaseBackButton class="back-button" />
  <AuthWrapper>
    <AppForm
      v-if="verificationStatus === 'idle'"
      title="Проверьте вашу почту"
      description="На почту, привязанную к аккаунту, отправлено письмо со ссылкой для активации организации. Если письма нет, проверьте папку «Спам»."
    >
      <NuxtIcon
        name="material-symbols:mark-email-read-outline-rounded"
        size="160px"
        style="color: var(--text-inverted-color)"
      />
    </AppForm>

    <AppForm
      v-else-if="verificationStatus === 'success'"
      title="Верификация успешна"
      description="Аккаунт организации успешно активирован."
    >
      <NuxtIcon
        name="material-symbols:sentiment-satisfied-rounded"
        size="160px"
        style="color: var(--text-inverted-color)"
      />
      <p class="verify__timer">Вы будете направлены на главную страницу через... {{ countdown }}</p>
    </AppForm>

    <AppForm v-else title="Ошибка верификации" :description="verificationErrorMessage">
      <NuxtIcon
        name="material-symbols:sentiment-sad-outline-rounded"
        size="160px"
        style="color: var(--text-inverted-color)"
      />
      <p class="verify__timer">Вы будете направлены на главную страницу через... {{ countdown }}</p>
    </AppForm>
  </AuthWrapper>
</template>

<style lang="scss" scoped>
.verify {
  &__timer {
    margin-top: 12px;
    font-size: 16px;
    font-weight: 600;
    color: var(--secondary-color);
  }
}

.back-button {
  top: 50px;
  left: 100px;
  position: absolute;
}
</style>
