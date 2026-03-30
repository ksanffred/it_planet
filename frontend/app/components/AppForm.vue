<script lang="ts" setup>
interface Props {
  title: string
  description: string
}

const props = defineProps<Props>()
</script>
<template>
  <div class="auth-card bordered">
    <BaseAppLogo class="auth-card__logo" />
    <hgroup class="auth-card__title-group">
      <h1 class="auth-card__title">{{ title }}</h1>
      <p class="auth-card__description">
        {{ description }}
      </p>
    </hgroup>
    <slot></slot>
  </div>
</template>
<style lang="scss" scoped>
.auth-card {
  border-radius: 32px;
  padding: 34px 34px 30px 34px;
  width: 460px;
  display: flex;
  flex-direction: column;
  gap: 20px;

  &__title-group {
    color: var(--text-inverted-color);
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  &__title {
    font-size: 34px;
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 800;
  }

  &__logo {
    color: var(--secondary-color);
    font-size: 16px;

    .dark & {
      color: var(--secondary-color);
    }
  }
}
</style>
