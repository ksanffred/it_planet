<script setup lang="ts"></script>

<template>
  <header class="app-hero">
    <div class="app-hero__left">
      <hgroup class="app-hero__title-group">
        <h1 class="app-hero__title">
          Найдите стажировки, вакансии, наставников и карьерные события в одной интерактивной ленте
        </h1>
        <p class="app-hero__description">
          Исследуйте возможности по стеку, уровню и формату. Мгновенно переключайтесь между картой и
          списком.
        </p>
      </hgroup>
      <div class="app-hero__tags">
        <UiAppTag background-color="var(--secondary-color)">Карта + лента</UiAppTag>
        <UiAppTag background-color="var(--background-secondary-color)">Умные теги</UiAppTag>
      </div>
    </div>
    <img class="app-hero__art" src="/media/images/heroArt.webp" alt="Hero art" />
  </header>
</template>

<style lang="scss" scoped>
.app-hero {
  display: flex;
  align-items: center;

  background-color: var(--primary-color);
  padding: 20px;
  margin: 12px 36px 24px 36px;
  border-radius: 26px;
  gap: 20px;

  &__title {
    font-size: 38px;
    width: fit-content;
    font-family: 'Plus Jakarta Sans', sans-serif;
    line-height: 1.05;
  }

  &__description {
    font-size: 16px;
    font-family: 'Inter', sans-serif;
    font-weight: 500;
    line-height: 1.35;
  }

  &__title-group {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  &__left {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  &__tags {
    display: flex;
    gap: 8px;
  }
}
</style>
