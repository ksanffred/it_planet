<script setup lang="ts"></script>

<template>
  <div class="opportunities-section container">
    <div class="opportunities-section__grid">
      <!-- <BaseOpportunityCard
        class="opportunities-section__card"
        v-for="item in []"
        :key="item.id"
        v-bind="item"
      /> -->
    </div>
  </div>
</template>

<style lang="scss" scoped>
.opportunities-section {
  padding: 24px 0;

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 40px;
  }
}
</style>
