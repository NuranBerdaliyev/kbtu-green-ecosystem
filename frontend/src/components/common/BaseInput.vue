<script setup>
import { useId } from 'vue'

defineProps({
  label: { type: String, default: '' },
  type: { type: String, default: 'text' },
  placeholder: { type: String, default: '' },
  error: { type: String, default: '' },
  autocomplete: { type: String, default: 'off' },
})

const model = defineModel({ type: String, default: '' })
const id = useId()
</script>

<template>
  <div class="field">
    <label v-if="label" :for="id">{{ label }}</label>
    <input
      :id="id"
      v-model="model"
      :type="type"
      :placeholder="placeholder"
      :autocomplete="autocomplete"
      :aria-invalid="Boolean(error)"
    />
    <p v-if="error" class="field__error">{{ error }}</p>
  </div>
</template>

<style scoped>
.field {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

label {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--c-ink-soft);
}

input {
  padding: var(--space-3);
  background: var(--c-surface);
  border: 1px solid var(--c-line-strong);
  border-radius: var(--radius-sm);
}

input:focus {
  border-color: var(--c-moss);
}

input[aria-invalid='true'] {
  border-color: var(--c-danger);
}

.field__error {
  font-size: var(--text-sm);
  color: var(--c-danger);
}
</style>
