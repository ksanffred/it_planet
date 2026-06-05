<script setup lang="ts">
interface Props {
  backgroundColor?: string
  bordered?: boolean
  textColor?: string
}

const props = defineProps<Props>()

const tagColors = [
  'var(--primary-color)',
  'var(--secondary-color)',
  'var(--tertiary-color)',
  'var(--background-secondary-color)',
]

const lightBackground = 'var(--background-secondary-color)'

const resolvedBg = ref(props.backgroundColor ?? tagColors[0])

const resolvedTextColor = computed(() => {
  if (resolvedBg.value === lightBackground) {
    return 'var(--text-inverted-color)'
  }
  return props.textColor ?? 'inherit'
})

onMounted(() => {
  if (!props.backgroundColor) {
    resolvedBg.value = tagColors[Math.floor(Math.random() * tagColors.length)]
  }
})
</script>

<template>
  <span
    :class="['app-tag', { bordered: bordered }]"
    :style="{
      backgroundColor: resolvedBg,
      color: resolvedTextColor,
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
