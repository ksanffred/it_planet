<script setup lang="ts">
const colorMode = useColorMode()

const toggleTheme = () => {
  colorMode.preference = colorMode.value === 'dark' ? 'light' : 'dark'
}
</script>
<template>
  <div class="container">
    <p id="test">Main Page</p>
    <button @click="toggleTheme">
      {{ colorMode.value === 'dark' ? 'Light' : 'Dark' }}
    </button>
  </div>
</template>

<style lang="scss" scoped>
#test {
  font-size: 50px;
  font-weight: 600;
}
</style>
