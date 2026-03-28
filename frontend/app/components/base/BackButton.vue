<script lang="ts" setup>
const router = useRouter()
</script>

<template>
  <BaseAppButton variant="primary" @click="router.back()">
    <NuxtIcon name="material-symbols:arrow-back-rounded" size="14px" />
    Вернуться назад</BaseAppButton
  >
</template>

<style lang="scss" scoped></style>
