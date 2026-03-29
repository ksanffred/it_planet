<script setup lang="ts"></script>

<template>
  <div class="search-section container">
    <BaseAppInput placeholder="Поиск стажировок, компаний, технологий..." />
    <BaseAppButton variant="secondary" class="search-section__map-button">
      <img
        class="search-section__map-thumb"
        src="/media/images/mapThumb.webp"
        alt="Map demonstration"
      />
      <div class="search-section__text">
        <span class="search-section__text-title">Открыть карту</span>
        <span class="search-section__text-subtitle">Смотрите возможности по локации</span>
      </div>
    </BaseAppButton>
  </div>
</template>

<style lang="scss" scoped>
.search-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;

  &__map-button {
    display: flex;
    align-items: center;
    min-width: fit-content;
    color: var(--text-primary-color) !important;
    background-color: var(--background-tertiary-color);
    border-radius: 20px;
    gap: 10px;
    padding-block: 6px;
    padding-inline: 10px;
  }

  &__map-thumb {
    border-radius: 12px;
    border: 1px solid var(--text-primary-color);

    .dark & {
      border: 1px solid var(--border-color);
    }
  }

  &__text {
    display: flex;
    text-align: left;
    text-wrap: nowrap;
    flex-direction: column;
    gap: 4px;

    &-title {
      font-family: 'Plus Jakarta Sans', sans-serif;
      font-size: 16px;
      font-weight: 800;
    }

    &-subtitle {
      font-family: 'Inter', sans-serif;
      font-size: 12px;
      font-weight: 500;
    }
  }
}
</style>
