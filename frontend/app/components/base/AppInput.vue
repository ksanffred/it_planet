<script setup lang="ts">
const model = defineModel()

defineProps<{ type?: string }>()
</script>

<template>
  <input class="app-input reset-input bordered" :type="type || 'text'" v-model="model" />
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
