<script setup lang="ts">
const showMap = ref(false)

const toggleMap = (value: boolean) => {
  showMap.value = value
}
</script>

<template>
  <div class="container">
    <SectionsHeroSection />

    <main>
      <SectionsSearchSection @update:show-map="toggleMap" />

      <template v-if="showMap"> </template>
      <template v-else>
        <SectionsOpportunitiesSection />
      </template>
    </main>
  </div>
</template>

<style lang="scss" scoped></style>
