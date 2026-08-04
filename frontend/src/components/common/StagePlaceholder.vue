<script setup>
defineProps({
  title: { type: String, required: true },
  stage: { type: String, required: true },
  owner: { type: String, default: '' },
  description: { type: String, default: '' },
})
</script>

<template>
  <section class="placeholder card">
    <p class="eyebrow">{{ stage }}</p>
    <h1>{{ title }}</h1>
    <p v-if="description" class="text-muted">{{ description }}</p>
    <p v-if="owner" class="placeholder__owner">Реализация: {{ owner }}</p>
  </section>
</template>

<style scoped>
.placeholder {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  border-style: dashed;
  background: var(--c-surface-sunk);
}

.placeholder__owner {
  font-size: var(--text-sm);
  color: var(--c-ink-muted);
}
</style>
