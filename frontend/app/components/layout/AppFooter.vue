<script setup lang="ts"></script>

<template>
  <footer class="app-footer">
    <nav class="app-footer__nav container">
      <BaseAppLogo />

      <div class="app-footer__nav-actions">
        <NuxtLink class="reset-link" to="/">Возможности</NuxtLink>
        <NuxtLink class="reset-link" to="/">Менторы</NuxtLink>
        <NuxtLink class="reset-link" to="/">События</NuxtLink>
        <NuxtLink class="reset-link" to="/">Контакты</NuxtLink>
      </div>
    </nav>
    <div class="app-footer__bottom container">
      <span class="app-footer__caption"
        >Поиск IT-карьеры на карте и в списке: стажировки, вакансии, менторство и события.</span
      >
      <span class="app-footer__bottom-copyright">2026 Tramplin</span>
    </div>
  </footer>
</template>

<style lang="scss" scoped>
.app-footer {
  background-color: var(--background-tertiary-color);
  padding: 24px;
  margin-top: 100vh;

  &__nav {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    &-actions {
      display: flex;
      gap: 18px;
      color: var(--background-secondary-color);

      .dark & {
        color: var(--text-tertiary-color);
      }
    }
  }

  &__bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;

    &-copyright {
      color: var(--secondary-color);
    }
  }
}
</style>
