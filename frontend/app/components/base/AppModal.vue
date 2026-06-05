<script lang="ts" setup>
defineProps<{ visible: boolean; title: string; confirmText?: string }>()
const emit = defineEmits<{ confirm: []; cancel: [] }>()

const onKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape') emit('cancel')
}

onMounted(() => document.addEventListener('keydown', onKeydown))
onUnmounted(() => document.removeEventListener('keydown', onKeydown))
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click.self="emit('cancel')">
        <div class="modal-card">
          <header class="modal-header">
            <h2 class="modal-title">{{ title }}</h2>
            <button
              class="modal-close-btn"
              type="button"
              @click="emit('cancel')"
              aria-label="Закрыть"
            >
              <NuxtIcon name="material-symbols:close-rounded" size="20px" />
            </button>
          </header>
          <div class="modal-body">
            <slot />
          </div>
          <footer class="modal-footer">
            <BaseAppButton variant="secondary" type="button" @click="emit('cancel')"
              >Отмена</BaseAppButton
            >
            <BaseAppButton variant="primary" type="button" @click="emit('confirm')">{{
              confirmText ?? 'Сохранить'
            }}</BaseAppButton>
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 16px;
}

.modal-card {
  background-color: var(--background-secondary-color);
  border-radius: 18px;
  width: 100%;
  max-width: 480px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px 0;
}

.modal-title {
  margin: 0;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 20px;
  font-weight: 800;
  color: var(--text-inverted-color);
  line-height: 1.2;
}

.modal-close-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: var(--text-tertiary-color);
  cursor: pointer;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: var(--background-tertiary-color);
  }
}

.modal-body {
  padding: 16px 18px;
    overflow: visible;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 0 18px 16px;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;

  .modal-card {
    transition: transform 0.2s ease;
  }
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;

  .modal-card {
    transform: scale(0.95);
  }
}
</style>
