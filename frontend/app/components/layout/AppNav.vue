<script setup lang="ts">
const colorMode = useColorMode()

const isDark = computed({
  get: () => colorMode.value === 'dark',
  set: () => {
    colorMode.preference = colorMode.value === 'dark' ? 'light' : 'dark'
  },
})
</script>

<template>
  <nav class="app-nav container">
    <UiAppLogo dot tag />

    <div class="app-nav__actions">
      <UiAppButton
        class="app-nav__theme-toggle"
        @click="isDark = !isDark"
        :aria-label="isDark ? 'Switch to light theme' : 'Switch to dark theme'"
        variant="secondary"
        bordered
      >
        <IconsSunIcon v-if="!isDark" />
        <IconsMoonIcon v-else />
      </UiAppButton>
      <UiAppButton variant="secondary" bordered>Войти</UiAppButton>
      <UiAppButton variant="primary">Регистрация</UiAppButton>
    </div>
  </nav>
</template>

<style lang="scss" scoped>
.app-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 24px;
  padding-bottom: 12px;
  padding-inline: 36px;

  &__actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__theme-toggle {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px;
  }
}
</style>
