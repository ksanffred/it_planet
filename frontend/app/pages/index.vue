<script setup lang="ts">
const route = useRoute()
const router = useRouter()

const showMap = computed(() => route.query.view === 'map')

const toggleMap = (value: boolean) => {
  router.push({
    query: {
      ...route.query,
      view: value ? 'map' : undefined,
    },
  })
}
</script>

<template>
  <div class="container">
    <SectionsHeroSection />

    <main>
      <SectionsSearchSection :show-map="showMap" @update:show-map="toggleMap" />

      <template v-if="showMap">
        <SectionsMapSection />
      </template>
      <template v-else>
        <SectionsOpportunitiesSection />
      </template>
    </main>
  </div>
</template>

<style lang="scss" scoped></style>
