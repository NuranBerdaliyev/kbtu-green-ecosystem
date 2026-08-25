<script setup>
import { computed } from 'vue'
import { TRIP_STATUS_LABELS, JOB_STATUS_LABELS, DEPOSIT_STATUS_LABELS } from '@/utils/constants'

const props = defineProps({
  status: { type: String, required: true },
  kind: { type: String, default: 'trip' }, // trip | job | deposit
})

const LABELS = {
  trip: TRIP_STATUS_LABELS,
  job: JOB_STATUS_LABELS,
  deposit: DEPOSIT_STATUS_LABELS,
}

const label = computed(() => LABELS[props.kind]?.[props.status] ?? props.status)

const tone = computed(
  () =>
    ({
      CREATED: 'neutral',
      PUBLISHED: 'go',
      IN_PROGRESS: 'go',
      COMPLETED: 'done',
      CANCELLED: 'stop',
      PENDING: 'neutral',
      REVIEWED: 'go',
      ACCEPTED: 'done',
      REJECTED: 'stop',
      APPROVED: 'done',
    })[props.status] ?? 'neutral',
)
</script>

<template>
  <span :class="['badge', `badge--${tone}`]">{{ label }}</span>
</template>

<style scoped>
.badge {
  display: inline-block;
  padding: 2px var(--space-2);
  border-radius: 999px;
  font-size: var(--text-xs);
  font-weight: 600;
  white-space: nowrap;
}

.badge--neutral {
  background: var(--c-surface-sunk);
  color: var(--c-ink-soft);
}
.badge--go {
  background: var(--c-coin-soft);
  color: var(--c-coin);
}
.badge--done {
  background: var(--c-moss-soft);
  color: var(--c-moss-dark);
}
.badge--stop {
  background: var(--c-danger-soft);
  color: var(--c-danger);
}
</style>
