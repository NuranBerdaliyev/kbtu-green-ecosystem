<script setup>
import { onMounted, ref } from 'vue'
import { gamificationApi } from '@/api/gamification'
import { useAuthStore } from '@/stores/auth'
import { useAsync } from '@/composables/useAsync'
import { formatNumber, formatCo2 } from '@/utils/format'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'

const auth = useAuthStore()
const page = ref(0)
const board = useAsync(() => gamificationApi.leaderboard(page.value, 20))

function go(delta) {
  page.value += delta
  board.run()
}

onMounted(board.run)
</script>

<template>
  <div class="stack">
    <PageHeader
      eyebrow="Gamification"
      title="Рейтинг"
      subtitle="Студенты и сотрудники, отсортированные по ESG-рейтингу."
    />

    <StateBlock
      :loading="board.loading.value"
      :error="board.error.value ?? ''"
      :empty="(board.data.value?.content ?? []).length === 0"
      empty-title="Рейтинг пуст"
      @retry="board.run"
    >
      <div class="card table-wrap">
        <table>
          <thead>
            <tr>
              <th>#</th>
              <th>Участник</th>
              <th class="num">ESG</th>
              <th class="num">EcoCoins</th>
              <th class="num">CO₂</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="entry in board.data.value.content"
              :key="entry.userId"
              :class="{ me: entry.userId === auth.userId }"
            >
              <td class="metric rank">{{ entry.rank }}</td>
              <td>
                {{ entry.fullName }}
                <span v-if="entry.userId === auth.userId" class="you">вы</span>
              </td>
              <td class="metric num">{{ entry.esgRating }}</td>
              <td class="metric num">{{ formatNumber(entry.ecoCoinsBalance) }}</td>
              <td class="metric num">{{ formatCo2(entry.totalCo2Saved) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <nav v-if="board.data.value.totalPages > 1" class="pager">
        <button type="button" :disabled="page === 0" @click="go(-1)">Назад</button>
        <span class="metric">{{ page + 1 }} / {{ board.data.value.totalPages }}</span>
        <button
          type="button"
          :disabled="page + 1 >= board.data.value.totalPages"
          @click="go(1)"
        >
          Вперёд
        </button>
      </nav>
    </StateBlock>
  </div>
</template>

<style scoped>
.table-wrap {
  padding: 0;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: var(--space-3) var(--space-4);
  text-align: left;
  border-bottom: 1px solid var(--c-line);
}

th {
  font-size: var(--text-xs);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--c-ink-muted);
  font-weight: 600;
}

.num {
  text-align: right;
}

.rank {
  color: var(--c-ink-muted);
  width: 3rem;
}

tr.me {
  background: var(--c-moss-soft);
}

tr.me td {
  font-weight: 600;
}

.you {
  margin-left: var(--space-2);
  padding: 1px var(--space-2);
  background: var(--c-moss);
  color: #fff;
  border-radius: 999px;
  font-size: var(--text-xs);
}

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  padding-top: var(--space-4);
}

.pager button {
  padding: var(--space-2) var(--space-4);
  border: 1px solid var(--c-line-strong);
  border-radius: var(--radius-sm);
  background: var(--c-surface);
  cursor: pointer;
}

.pager button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
