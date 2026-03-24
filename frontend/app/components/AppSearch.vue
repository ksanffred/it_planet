<script setup lang="ts"></script>

<template>
  <div class="app-search container">
    <UiAppInput placeholder="Поиск стажировок, компаний, технологий..." />
    <UiAppButton variant="secondary" class="app-search__map-button">
      <img
        class="app-search__map-thumb bordered"
        src="/media/images/mapThumb.webp"
        alt="Map demonstration"
      />
      <div class="app-search__text">
        <span class="app-search__text-title">Открыть карту</span>
        <span class="app-search__text-subtitle">Смотрите возможности по локации</span>
      </div>
    </UiAppButton>
  </div>
</template>

<style lang="scss" scoped>
.app-search {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;

  &__map-button {
    display: flex;
    align-items: center;

    border-radius: 20px;
    gap: 10px;
    padding-block: 6px;
    padding-inline: 10px;
  }

  &__map-thumb {
    border-radius: 12px;
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
