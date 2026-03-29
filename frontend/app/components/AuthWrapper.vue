<script lang="ts" setup></script>
<template>
  <div class="auth-wrapper">
    <NuxtImg src="/media/images/loginPanel.webp" />
    <slot></slot>
  </div>
</template>
<style lang="scss" scoped>
.auth-wrapper {
  display: flex;
  width: 100vw;
  height: 100vh;
  align-items: center;
  gap: 15px;
  justify-content: center;
}
</style>
