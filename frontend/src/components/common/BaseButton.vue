<script setup>
defineProps({
  variant: { type: String, default: 'primary' }, // primary | ghost | danger
  type: { type: String, default: 'button' },
  disabled: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
})
</script>

<template>
  <button :type="type" :class="['btn', `btn--${variant}`]" :disabled="disabled || loading">
    <span v-if="loading" class="btn__spinner" aria-hidden="true" />
    <slot />
  </button>
</template>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  transition:
    background-color 0.15s ease,
    border-color 0.15s ease;
}

.btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.btn--primary {
  background: var(--c-moss);
  color: #fff;
}
.btn--primary:hover:not(:disabled) {
  background: var(--c-moss-dark);
}

.btn--ghost {
  background: transparent;
  border-color: var(--c-line-strong);
  color: var(--c-ink);
}
.btn--ghost:hover:not(:disabled) {
  background: var(--c-surface-sunk);
}

.btn--danger {
  background: var(--c-danger);
  color: #fff;
}

.btn__spinner {
  width: 14px;
  height: 14px;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
