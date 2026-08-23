<script setup>
import { onMounted } from 'vue'
import { gamificationApi } from '@/api/gamification'
import { useAsync } from '@/composables/useAsync'
import { formatDateTime } from '@/utils/format'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'

// The backend returns every achievement with an `unlocked` flag,
// so locked ones are shown too — they read as goals rather than gaps.
const achievements = useAsync(gamificationApi.achievements, [])

onMounted(achievements.run)
</script>

<template>
  <div class="stack">
    <PageHeader
      eyebrow="Gamification"
      title="Достижения"
      subtitle="Открываются автоматически за экологическую активность."
    />

    <StateBlock
      :loading="achievements.loading.value"
      :error="achievements.error.value ?? ''"
      :empty="(achievements.data.value ?? []).length === 0"
      empty-title="Достижений нет"
      @retry="achievements.run"
    >
      <ul class="grid">
        <li
          v-for="item in achievements.data.value"
          :key="item.code"
          :class="['card', 'achievement', { 'achievement--locked': !item.unlocked }]"
        >
          <span class="achievement__mark" aria-hidden="true" />
          <div>
            <h3>{{ item.title }}</h3>
            <p class="text-muted">{{ item.description }}</p>
            <p v-if="item.unlocked" class="achievement__date">
              Получено {{ formatDateTime(item.unlockedAt) }}
            </p>
          </div>
        </li>
      </ul>
    </StateBlock>
  </div>
</template>

<style scoped>
.grid {
  list-style: none;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-4);
}

.achievement {
  display: flex;
  gap: var(--space-3);
  align-items: flex-start;
}

.achievement__mark {
  flex: none;
  width: 14px;
  height: 14px;
  margin-top: 5px;
  border-radius: 50% 0 50% 50%;
  background: var(--c-moss);
}

.achievement--locked {
  background: var(--c-surface-sunk);
  border-style: dashed;
}

.achievement--locked .achievement__mark {
  background: var(--c-line-strong);
}

.achievement--locked h3,
.achievement--locked p {
  color: var(--c-ink-muted);
}

h3 {
  font-size: var(--text-base);
  margin-bottom: var(--space-1);
}

.achievement__date {
  margin-top: var(--space-2);
  font-size: var(--text-xs);
  color: var(--c-moss-dark);
}
</style>
