<script setup lang="ts">
const route = useRoute();
const router = useRouter();

const showMap = computed(() => route.query.view === "map");

const toggleMap = (value: boolean) => {
  router.push({
    query: {
      ...route.query,
      view: value ? "map" : undefined,
    },
  });
};
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
        <SvgArcShape class="arc-shape" />
        <SvgCircleShape class="circle-shape" />
        <SvgBlockShape class="block-shape" />

        <SectionsOpportunitiesSection />
      </template>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.arc-shape {
  position: absolute;
  transform: translate(200px, 750px) rotate(20deg);

  width: 184px;
  height: 92px;
}

.circle-shape {
  position: absolute;
  transform: translate(700px, 300px);

  width: 150px;
  height: 150px;
}

.block-shape {
  position: absolute;
  transform: translate(700px, 1100px);

  width: 164px;
  height: 164px;
}
</style>
