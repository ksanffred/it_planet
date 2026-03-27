<script setup lang="ts">
import type { OpportunityCard } from '@/types/opportunityCard'

const { title, description, image, type, tags } = defineProps<OpportunityCard>()
</script>

<template>
  <div :class="['opportunity-card', `opportunity-card--${type}`, type === 'job' ? 'bordered' : '']">
    <NuxtImg
      class="opportunity-card__image"
      :src="'/media/images/heroArt.webp'"
      lazy
      alt="Opportunity image"
    />
    <h2 class="opportunity-card__title">{{ title }}</h2>
    <p class="opportunity-card__description">{{ description }}</p>
    <div class="opportunity-card__tags">
      <span v-for="tag in tags" :key="tag">{{ tag }}</span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.opportunity-card {
  $b: #{&};
  padding: 12px;
  border-radius: 22px;
  color: var(--text-inverted-color);

  &__image {
    width: 100%;
    border-radius: 16px;
  }
  &__title {
    font-family: 'Plus Jakarta Sans', sans-serif;
    text-transform: capitalize;
    line-height: 1.1;
    font-weight: 800;
    font-size: 18px;
  }

  &__description {
    font-family: 'Inter', sans-serif;
    font-size: 13px;
    font-weight: 500;
    line-height: 1.35;
  }

  &__tags {
    color: var(--primary-color);
    font-family: 'Inter', sans-serif;
    font-size: 12px;
    font-weight: 700;

    .dark & {
      color: #c3d1e6 !important;
    }

    #{$b}--internship & {
      color: var(--background-secondary-color);
    }

    #{$b}--event & {
      color: var(--background-secondary-color);
    }

    ::after {
      content: ' · ';
    }

    :last-child::after {
      content: '';
    }
  }

  &--job {
    background-color: var(--background-secondary-color);
  }
  &--internship {
    background-color: var(--tertiary-color);
    color: var(--text-primary-color);
  }
  &--event {
    color: var(--text-primary-color);

    background-color: var(--primary-color);
  }
}
</style>
