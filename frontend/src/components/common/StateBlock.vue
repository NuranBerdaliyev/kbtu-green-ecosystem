<script setup>
defineProps({
  loading: Boolean,
  error: { type: String, default: '' },
  empty: Boolean,
  emptyTitle: { type: String, default: 'Пока пусто' },
  emptyText: { type: String, default: '' },
  skeletons: { type: Number, default: 3 },
})
defineEmits(['retry'])
</script>

<template>
  <div v-if="loading" class="skeletons">
    <div v-for="n in skeletons" :key="n" class="skeleton" />
  </div>

  <div v-else-if="error" class="card state state--error">
    <p class="state__title">Не удалось загрузить</p>
    <p class="text-muted">{{ error }}</p>
    <button class="state__retry" type="button" @click="$emit('retry')">Повторить</button>
  </div>

  <div v-else-if="empty" class="card state">
    <p class="state__title">{{ emptyTitle }}</p>
    <p v-if="emptyText" class="text-muted">{{ emptyText }}</p>
    <slot name="empty-action" />
  </div>

  <slot v-else />
</template>

<style scoped>
.skeletons {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.skeleton {
  height: 92px;
  border-radius: var(--radius);
  background: linear-gradient(
    90deg,
    var(--c-surface-sunk) 25%,
    #e8ebe4 50%,
    var(--c-surface-sunk) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

@keyframes shimmer {
  to {
    background-position: -200% 0;
  }
}

.state {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-3);
  text-align: left;
}

.state__title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 600;
}

.state--error {
  border-color: var(--c-danger);
  background: var(--c-danger-soft);
}

.state__retry {
  padding: var(--space-2) var(--space-4);
  border: 1px solid var(--c-line-strong);
  border-radius: var(--radius-sm);
  background: var(--c-surface);
  font-weight: 600;
  cursor: pointer;
}
</style>
