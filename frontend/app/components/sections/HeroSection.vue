<script setup lang="ts"></script>

<template>
  <header class="hero-section">
    <div class="hero-section__left">
      <hgroup class="hero-section__title-group">
        <h1 class="hero-section__title">
          Найдите стажировки, вакансии, наставников и карьерные события в одной интерактивной ленте
        </h1>
        <p class="hero-section__description">
          Исследуйте возможности по стеку, уровню и формату. Мгновенно переключайтесь между картой и
          списком.
        </p>
      </hgroup>
      <div class="hero-section__tags">
        <BaseAppTag background-color="var(--secondary-color)">Карта + лента</BaseAppTag>
        <BaseAppTag
          background-color="var(--background-secondary-color)"
          text-color="var(--text-inverted-color)"
          >Умные теги</BaseAppTag
        >
      </div>
    </div>
    <img class="hero-section__art" src="/media/images/heroArt.webp" alt="Hero art" />
  </header>
</template>

<style lang="scss" scoped>
.hero-section {
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
