<script setup lang="ts"></script>
<template>
  <div class="container">
    <AppHero />
    <AppSearch />
    <UiOpportunityCard
      :tags="['gggg', 'gadfasda', 'asdass']"
      title="adasdsa"
      description="adsadsada"
      image="adsadsa"
      type="job"
    />
    <UiOpportunityCard
      :tags="['gggg', 'gadfasda', 'asdass']"
      title="adasdsa"
      description="adsadsada"
      image="adsadsa"
      type="internship"
    />
    <UiOpportunityCard
      :tags="['gggg', 'gadfasda', 'asdass']"
      title="adasdsa"
      description="adsadsada"
      image="adsadsa"
      type="event"
    />
  </div>
</template>

<style lang="scss" scoped></style>
