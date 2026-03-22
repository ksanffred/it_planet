<script setup lang="ts">
interface Props {
  variant: 'primary' | 'secondary'
}
const { variant } = defineProps<Props>()
</script>

<template>
  <button :class="['reset-button', 'app-button', `app-button--${variant}`]">
    <slot></slot>
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
  }

  .light &--secondary {
    color: var(--text-inverted-color);
  }
}
</style>
