<script setup lang="ts">
import type { OpportunityMiniCard } from '@/types/opportunity'

type OpportunityCardProps = OpportunityMiniCard & {
  isFavorite?: boolean
  isFavoriteLoading?: boolean
}

const {
  id,
  media,
  title,
  description,
  employerName,
  format,
  type,
  tags,
  isFavorite = false,
  isFavoriteLoading = false,
} = defineProps<OpportunityCardProps>()

const emit = defineEmits<{
  'toggle-favorite': [id: number]
}>()

const handleToggleFavorite = () => {
  emit('toggle-favorite', id)
}
</script>

<template>
  <div
    :class="[
      'opportunity-card',
      `opportunity-card--${formatOpporunityType(type, 'en')}`,
      formatOpporunityType(type, 'ru') === 'Вакансия' ? 'bordered' : '',
    ]"
  >
    <button
      class="opportunity-card__favorite"
      type="button"
      :aria-pressed="isFavorite"
      :aria-label="
        isFavorite ? 'Удалить возможность из избранного' : 'Добавить возможность в избранное'
      "
      :disabled="isFavoriteLoading"
      @click.stop="handleToggleFavorite"
    >
      <NuxtIcon
        :name="
          isFavorite ? 'material-symbols:star-rounded' : 'material-symbols:star-outline-rounded'
        "
        size="22px"
      />
    </button>

    <NuxtLink :to="`opportunities/${id}`" class="opportunity-card__image-wrapper">
      <NuxtIcon class="opportunity-card__icon" name="material-symbols:open-in-new" size="24px" />
      <NuxtImg
        class="opportunity-card__image"
        :src="media === 'string' ? '/media/images/heroArt.webp' : media"
        lazy
        alt="Opportunity image"
        fit="cover"
      />
    </NuxtLink>

    <h2 class="opportunity-card__title">{{ title }}</h2>
    <p class="opportunity-card__description">{{ description }}</p>
    <div class="opportunity-card__tags">
      <span> {{ employerName }}</span>
      <span> {{ formatOpporunityFormat(format, 'ru') }}</span>
      <span v-for="tag in tags" :key="tag">{{ tag }}</span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.opportunity-card {
  $b: #{&};
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  border-radius: 22px;
  color: var(--text-inverted-color);
  position: relative;

  &__icon {
    position: absolute;
    right: 20px;
    top: 20px;
    color: var(--text-inverted-color);

    .dark & {
      color: var(--background-primary-color);
    }
  }

  &__favorite {
    position: absolute;
    left: 20px;
    top: 20px;
    z-index: 2;
    border: none;
    background: transparent;
    color: #f6bc30;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    line-height: 1;

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }

  &__image-wrapper {
    flex-grow: 1;
  }

  &__image {
    width: 100%;
    height: 100%;
    border-radius: 16px;
    object-fit: cover;
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

    #{$b}--Internship & {
      color: var(--background-secondary-color);
    }

    #{$b}--Event & {
      color: var(--background-secondary-color);
    }

    ::after {
      content: ' · ';
    }

    :last-child::after {
      content: '';
    }
  }

  &--Vacancy {
    background-color: var(--background-secondary-color);
  }
  &--Internship {
    background-color: var(--tertiary-color);
    color: var(--text-primary-color);
  }
  &--Event {
    color: var(--text-primary-color);

    background-color: var(--primary-color);
  }
}
</style>
