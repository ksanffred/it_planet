<script setup lang="ts">
const colorMode = useColorMode()

console.log('Current color mode:', colorMode.value)
console.log('Preference:', colorMode.preference)
</script>

<template>
  <div>
    <NuxtPage />
  </div>
</template>
