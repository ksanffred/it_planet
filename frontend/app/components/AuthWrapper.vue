<script lang="ts" setup>
const colorMode = useColorMode()
</script>
<template>
  <div class="auth-wrapper">
    <NuxtImg v-if="colorMode.value === 'light'" src="/media/images/loginPanel.webp" />
    <NuxtImg v-else="colorMode.value === 'dark'" src="/media/images/loginPanelDark.webp" />

    <slot></slot>
  </div>
</template>
<style lang="scss" scoped>
.auth-wrapper {
  display: flex;
  width: 100vw;
  height: 100vh;
  align-items: center;
  gap: 15px;
  justify-content: center;
}
</style>
