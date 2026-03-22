<script setup lang="ts">
interface Props {
  variant: 'primary' | 'secondary'
  to?: string
}
const { variant } = defineProps<Props>()
</script>

<template>
  <button :class="['reset-button', 'app-button', `app-button--${variant}`]">
    <NuxtLink class="reset-link" :to="to ? to : '/'">
      <slot></slot>
    </NuxtLink>
  </button>
</template>

<style lang="scss" scoped>
.app-button {
  &--primary {
    background-color: var(--primary-color);
  }

  &--secondary {
    background-color: var(--background-secondary-color);
    border: 1px solid var(--border-color);

    .light & {
      color: var(--text-inverted-color);
    }
  }
}
</style>
