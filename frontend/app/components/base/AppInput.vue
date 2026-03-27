<script setup lang="ts">
interface Props {
  placeholder?: string
}

const { placeholder } = defineProps<Props>()
</script>

<template>
  <input class="app-input reset-input bordered" type="text" :placeholder="placeholder" />
</template>

<style lang="scss" scoped>
.app-input {
  border-radius: 50vw;
  padding-block: 12px;
  padding-inline: 16px;
  background-color: var(--background-secondary-color);
  width: 100%;
  color: var(--text-inverted-color);
}
</style>
