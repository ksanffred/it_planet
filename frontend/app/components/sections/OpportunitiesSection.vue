<script setup lang="ts">
import { opportunities } from '~/data/opportunities'
</script>

<template>
  <div class="opportunities-section container">
    <div class="opportunities-section__grid">
      <BaseOpportunityCard v-for="item in opportunities" :key="item.id" v-bind="item" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.opportunities-section {
  padding: 24px 0;

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }
}
</style>
