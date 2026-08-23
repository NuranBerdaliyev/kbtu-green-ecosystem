<script setup>
import { computed } from 'vue'
import { FULLNESS_CRITICAL, FULLNESS_WARNING } from '@/utils/constants'

const props = defineProps({
  value: { type: Number, default: 0 },
})

const tone = computed(() => {
  if (props.value >= FULLNESS_CRITICAL) return 'critical'
  if (props.value >= FULLNESS_WARNING) return 'warning'
  return 'ok'
})
</script>

<template>
  <div class="fullness">
    <div
      class="fullness__track"
      role="progressbar"
      :aria-valuenow="value"
      aria-valuemin="0"
      aria-valuemax="100"
    >
      <div :class="['fullness__fill', `fullness__fill--${tone}`]" :style="{ width: `${value}%` }" />
    </div>
    <span class="metric fullness__label">{{ value }}%</span>
  </div>
</template>

<style scoped>
.fullness {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.fullness__track {
  flex: 1;
  height: 6px;
  border-radius: 999px;
  background: var(--c-surface-sunk);
  overflow: hidden;
}

.fullness__fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.4s ease;
}

.fullness__fill--ok {
  background: var(--c-moss);
}
.fullness__fill--warning {
  background: var(--c-coin);
}
.fullness__fill--critical {
  background: var(--c-danger);
}

.fullness__label {
  font-size: var(--text-sm);
  color: var(--c-ink-soft);
  min-width: 3.5ch;
  text-align: right;
}
</style>
