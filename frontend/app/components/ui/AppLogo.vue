<script setup lang="ts">
interface Props {
  dot?: boolean
  tag?: boolean
}

const { dot, tag } = defineProps<Props>()
</script>
n
<template>
  <NuxtLink class="app-logo" to="/">
    <span v-if="dot" class="app-logo__dot"></span>
    <span class="app-logo__text">трамплин</span>
    <span v-if="tag" class="app-logo__tag">платформа карьеры</span>
  </NuxtLink>
</template>

<style lang="scss" scoped>
.app-logo {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  text-decoration: none;
  gap: 12px;

  &__dot {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background-color: var(--secondary-color);
  }

  &__text {
    font-size: 30px;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 800;
    color: var(--text-inverted-color);
    text-transform: uppercase;

    .dark & {
      color: var(--text-primary-color);
    }
  }

  &__tag {
    font-size: 14px;
    font-weight: 600;
    color: var(--tertiary-color);
    text-transform: lowercase;

    .dark & {
      font-size: 14px;
      font-weight: 600;
      color: #6ee7b7;
    }
  }
}
</style>
