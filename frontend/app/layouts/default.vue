<script setup lang="ts"></script>

<template>
  <div class="app-layout">
    <LayoutAppNav />
    <main class="app-layout__content">
      <slot />
    </main>
    <LayoutAppFooter />
  </div>
</template>

<style lang="scss" scoped>
.app-layout {
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  width: 100%;
  align-items: stretch;

  > * {
    width: 100%;
  }

  &__content {
    flex: 1;
    min-height: 0;
  }
}
</style>
