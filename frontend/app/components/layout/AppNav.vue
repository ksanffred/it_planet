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
    <BaseAppLogo class="app-nav__logo" dot tag />

    <div class="app-nav__actions">
      <BaseAppButton
        class="app-nav__theme-toggle"
        @click="isDark = !isDark"
        :aria-label="isDark ? 'Switch to light theme' : 'Switch to dark theme'"
        variant="secondary"
        bordered
      >
        <NuxtIcon v-if="!isDark" size="24px" name="material-symbols:wb-sunny-rounded" />
        <NuxtIcon size="24px" name="material-symbols:moon-stars-rounded" v-else />
      </BaseAppButton>
      <BaseAppButton @click="navigateTo('/auth/login')" variant="secondary" bordered
        >Войти</BaseAppButton
      >
      <BaseAppButton @click="navigateTo('/auth/register')" variant="primary"
        >Регистрация</BaseAppButton
      >
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

  &__logo {
    color: var(--text-inverted-color);
    font-size: 30px;
  }

  &__actions {
    display: flex;
    align-items: center;
    justify-content: center;
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
