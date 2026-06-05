<script setup lang="ts">
const canGoBack = ref(false)

onMounted(() => {
  canGoBack.value = window.history.length > 1
})

const router = useRouter()

const handleClick = () => {
  if (canGoBack.value) {
    router.back()
  } else {
    navigateTo('/')
  }
}
</script>

<template>
  <button type="button" class="back-button" @click="handleClick">
    <NuxtIcon name="material-symbols:arrow-back-rounded" size="16px" />
    {{ canGoBack ? 'Вернуться назад' : 'Вернуться на главную' }}
  </button>
</template>

<style lang="scss" scoped>
.back-button {
  border: 0;
  background-color: var(--primary-color);
  color: #fff;
  border-radius: 999px;
  padding: 8px 14px;
  width: fit-content;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 700;
  cursor: pointer;
}
</style>
