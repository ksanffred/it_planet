<script setup lang="ts">
interface Props {
  backgroundColor?: string
  bordered?: boolean
  textColor?: string
}

const { backgroundColor, bordered, textColor } = defineProps<Props>()
</script>

<template>
  <span
    :class="['app-tag', { bordered: bordered }]"
    :style="{
      backgroundColor: backgroundColor ?? 'var(--primary-color)',
      color: textColor ? textColor : 'inherit',
    }"
  >
    <slot></slot>
  </span>
</template>

<style lang="scss" scoped>
.app-tag {
  display: inline-block;
  padding-block: 7px;
  padding-inline: 12px;
  font-weight: 700;
  font-size: 12px;
  text-transform: capitalize;
  border-radius: 50vw;
}
</style>
