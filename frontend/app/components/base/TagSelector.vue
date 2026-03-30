<script setup lang="ts">
import type { Tag } from '~/types'

interface Props {
  availableTags: Tag[]
  selectedTags: Tag[]
}

const { availableTags, selectedTags } = defineProps<Props>()

const emit = defineEmits<{
  'update:selectedTags': [value: Tag[]]
}>()

const isOpen = ref(false)

const toggleTag = (tag: Tag) => {
  const index = selectedTags.findIndex((t) => t.id === tag.id)
  if (index === -1) {
    emit('update:selectedTags', [...selectedTags, tag])
  } else {
    emit(
      'update:selectedTags',
      selectedTags.filter((_, i) => i !== index),
    )
  }
}

const isSelected = (tag: Tag) => selectedTags.some((t) => t.id === tag.id)

const toggleDropdown = () => {
  isOpen.value = !isOpen.value
}

const dropdownRef = ref<HTMLElement | null>(null)

const handleClickOutside = (event: MouseEvent) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="tag-selector" ref="dropdownRef">
    <BaseAppButton
      @click="toggleDropdown"
      variant="secondary"
      class="tag-selector__button"
      :class="{ 'tag-selector__button--active': isOpen }"
    >
      <span>Теги</span>
      <NuxtIcon
        name="material-symbols:keyboard-arrow-down"
        size="20px"
        :class="['tag-selector__icon', { 'tag-selector__icon--rotated': isOpen }]"
      />
    </BaseAppButton>

    <Transition name="slide">
      <div v-if="isOpen" class="tag-selector__dropdown">
        <div class="tag-selector__list">
          <button
            v-for="tag in availableTags"
            :key="tag.id"
            @click="toggleTag(tag)"
            class="tag-selector__item"
            :class="{ 'tag-selector__item--selected': isSelected(tag) }"
          >
            <span>{{ tag.name }}</span>
            <NuxtIcon
              v-if="isSelected(tag)"
              name="material-symbols:check-rounded"
              size="18px"
              class="tag-selector__check"
            />
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style lang="scss" scoped>
.tag-selector {
  position: relative;

  &__button {
    display: flex;
    align-items: center;
    gap: 6px;

    &--active {
      background-color: var(--primary-color);
      color: var(--text-inverse-color);
    }
  }

  &__icon {
    transition: transform 0.2s ease;

    &--rotated {
      transform: rotate(180deg);
    }
  }

  &__dropdown {
    position: absolute;
    top: calc(100% + 8px);
    right: 0;
    min-width: 200px;
    max-height: 300px;
    overflow-y: auto;
    background-color: var(--background-secondary-color);
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    z-index: 100;
    padding: 8px;
  }

  &__list {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    border-radius: 8px;
    border: none;
    background: transparent;
    cursor: pointer;
    font-family: 'Inter', sans-serif;
    font-size: 14px;
    color: var(--text-primary-color);
    transition: background-color 0.2s ease;

    &:hover {
      background-color: var(--background-tertiary-color);
    }

    &--selected {
      background-color: var(--primary-color);
      color: var(--text-inverse-color);
    }
  }

  &__check {
    flex-shrink: 0;
  }
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.2s ease;
}

.slide-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
