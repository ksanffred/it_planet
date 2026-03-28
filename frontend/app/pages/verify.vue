<script setup lang="ts">
definePageMeta({
  layout: false,
})
</script>

<template>
  <BaseBackButton class="back-button" />

  <AuthWrapper> </AuthWrapper>
</template>

<style lang="scss" scoped>
.back-button {
  top: 50px;
  left: 100px;
  position: absolute;
}
</style>
