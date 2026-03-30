<script lang="ts" setup>
interface Props {
  label: string
  id: string
  placeholder?: string
  type?: string
  required?: boolean
  minlength?: number
  maxlength?: number
  autocomplete?: boolean
  accept?: string
  min?: string
  max?: number
  value?: number
}

const props = defineProps<Props>()

const model = defineModel()
</script>
<template>
  <label class="form-label" :for="id">
    {{ label }}
    <BaseAppInput
      class="form-input"
      v-model="model"
      :id="id"
      :name="id"
      :minlength="minlength"
      :maxlength="maxlength"
      :type="type"
      :autocomplete="autocomplete"
      :placeholder="placeholder"
      :required="required"
      :accept="accept"
      :min="min"
      :max="max"
    />
  </label>
</template>
<style lang="scss" scoped>
.form-label {
  display: flex;
  align-items: start;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
  color: var(--text-inverted-color);

  font-weight: 700;
}

.form-input {
  &::file-selector-button {
    background-color: transparent;
    border: none;
    content: 'Select    ';
    font-family: 'Inter', sans-serif;
    color: var(--test-primary-color);
  }
}
</style>
