<script lang="ts" setup>
type Size = 'sm' | 'lg'

interface Props {
  avatar: string
  size: Size
  name: string
  subtitle: string
}

const sizeMap: Record<Size, Omit<Props, 'size'>> = {
  sm: { avatar: '42px', name: '13px', subtitle: '12px' },
  lg: { avatar: '72px', name: '24px', subtitle: '13px' },
}

const props = defineProps<Props>()
</script>
<template>
  <div class="user-identity">
    <NuxtImg
      class="user-identity__avatar"
      :src="avatar"
      :width="sizeMap[size].avatar + 'px'"
      :height="sizeMap[size].avatar + 'px'"
      :alt="'Аватар пользователя ' + name"
    />
    <div class="user-identity__content">
      <span :style="{ 'font-size': sizeMap[size].name }" class="user-identity__name">{{
        name
      }}</span>
      <span :style="{ 'font-size': sizeMap[size].subtitle }" class="user-identity__subtitle">{{
        subtitle
      }}</span>
    </div>
  </div>
</template>
<style lang="scss" scoped>
.user-identity {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;

  &__avatar {
    background-color: #dce9fa;
    border-radius: 50vw;
    width: 72px;
    height: 72px;
    aspect-ratio: 1/1;
    border: 1px solid var(--var-primary-color);
  }

  &__content {
    display: flex;
    flex-direction: column;
    color: var(--text-inverted-color);
  }

  &__name {
    font-family: 'Plus Jakarta Sans', sans-serif;
    font-weight: 800;
  }
}
</style>
