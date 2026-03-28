<script setup lang="ts">
const route = useRoute()
const token = route.query.token

if (!token || typeof token !== 'string') {
  navigateTo('/')
}

const config = useRuntimeConfig()
const countdown = ref(3)
let redirectPath = '/'

const { data, error, pending } = await useFetch(`${config.public.apiBase}/auth/verify`, {
  method: 'GET',
  query: { token },
  immediate: true,
})

if (data.value) {
  redirectPath = '/auth/login'
} else if (error.value) {
  redirectPath = '/'
}

if (!pending.value) {
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      navigateTo(redirectPath)
    }
  }, 1000)
}

definePageMeta({
  layout: false,
})
</script>

<template>
  <AuthWrapper>
    <AuthCard
      v-if="!pending && data"
      title="Email подтверждён"
      description="Ваш email успешно подтверждён. "
    >
      <NuxtIcon
        name="material-symbols:heart-smile-outline-rounded"
        size="256px"
        style="color: var(--text-inverted-color)"
      />
      <p class="verify__timer">
        Вы будете перенаправлены в личный кабинет через {{ countdown }} сек.
      </p>
    </AuthCard>
    <AuthCard
      v-else-if="!pending && error"
      title="Ошибка верификации"
      description="Неверный или истёкший токен."
    >
      <NuxtIcon
        name="material-symbols:sentiment-sad-outline-rounded"
        size="256px"
        style="color: var(--text-inverted-color)"
      />
      <p class="verify__timer">
        Вы будете перенаправлены на главную страницу через {{ countdown }} сек.
      </p>
    </AuthCard>
    <AuthCard v-else title="Проверка..." description="Пожалуйста, подождите"> </AuthCard>
  </AuthWrapper>
</template>

<style lang="scss" scoped>
.back-button {
  top: 50px;
  left: 100px;
  position: absolute;
}

.verify {
  &__timer {
    margin-top: 16px;
    font-size: 18px;
    font-weight: 600;
    color: var(--secondary-color);
  }
}
</style>
