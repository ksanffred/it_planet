<script setup lang="ts">
interface Props {
  variant: 'primary' | 'secondary'
  bordered?: boolean
  to?: string
}
const { variant } = defineProps<Props>()
</script>

<template>
  <button :class="['reset-button', { bordered: bordered }, 'app-button', `app-button--${variant}`]">
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

    .light & {
      color: var(--text-inverted-color);
    }
  }
}
</style>
