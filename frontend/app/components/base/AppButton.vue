<script setup lang="ts">
interface Props {
  variant: 'primary' | 'secondary'
  bordered?: boolean
  to?: string
}
const { variant, bordered } = defineProps<Props>()
</script>

<template>
  <button :class="['reset-button', { bordered: bordered }, 'app-button', `app-button--${variant}`]">
    <slot></slot>
  </button>
</template>

<style lang="scss" scoped>
.app-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;

  &--primary {
    background-color: var(--primary-color);

    &:disabled {
      background-color: var(--primary-color-disabled);
      cursor: not-allowed;
    }
  }

  &--secondary {
    background-color: var(--background-secondary-color);

    .light & {
      color: var(--text-inverted-color);
    }
  }
}
</style>
