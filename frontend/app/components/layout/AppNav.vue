<script setup lang="ts"></script>

<template>
  <nav class="app-nav container">
    <UiAppLogo dot tag />
    <div class="app-nav__actions">
      <UiAppButton variant="secondary">Войти</UiAppButton>
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
    gap: 12px;
  }
}
</style>
