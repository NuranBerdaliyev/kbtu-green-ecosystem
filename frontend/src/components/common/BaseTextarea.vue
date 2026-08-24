<script setup>
import { computed, useId } from 'vue'

const props = defineProps({
  label: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  error: { type: String, default: '' },
  rows: { type: Number, default: 6 },
  minLength: { type: Number, default: 0 },
  maxLength: { type: Number, default: 0 },
})

const model = defineModel({ type: String, default: '' })
const id = useId()

const counter = computed(() => {
  if (!props.maxLength) return ''
  return `${model.value.length} / ${props.maxLength}`
})

const tooShort = computed(
  () => props.minLength > 0 && model.value.length > 0 && model.value.length < props.minLength,
)
</script>

<template>
  <div class="field">
    <div class="field__top">
      <label v-if="label" :for="id">{{ label }}</label>
      <span v-if="counter" :class="['field__counter', { 'field__counter--warn': tooShort }]">
        {{ counter }}
      </span>
    </div>
    <textarea
      :id="id"
      v-model="model"
      :rows="rows"
      :placeholder="placeholder"
      :maxlength="maxLength || undefined"
      :aria-invalid="Boolean(error) || tooShort"
    />
    <p v-if="error" class="field__error">{{ error }}</p>
    <p v-else-if="tooShort" class="field__error">Минимум {{ minLength }} символов</p>
  </div>
</template>

<style scoped>
.field {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.field__top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

label {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--c-ink-soft);
}

.field__counter {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--c-ink-muted);
}

.field__counter--warn {
  color: var(--c-danger);
}

textarea {
  padding: var(--space-3);
  background: var(--c-surface);
  border: 1px solid var(--c-line-strong);
  border-radius: var(--radius-sm);
  resize: vertical;
  font-family: inherit;
}

textarea:focus {
  border-color: var(--c-moss);
}

textarea[aria-invalid='true'] {
  border-color: var(--c-danger);
}

.field__error {
  font-size: var(--text-sm);
  color: var(--c-danger);
}
</style>
