<script setup>
import { computed, onMounted } from 'vue'
import { wasteLogsAdminApi } from '@/api/ecoWaste'
import { useAsync } from '@/composables/useAsync'
import { formatNumber, formatDateTime } from '@/utils/format'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'

/**
 * WasteLogResponseDto currently exposes only id, userId, containerId, scannedAt
 * and ecoCoinsEarned — weight and added fullness are not stored, so a full
 * audit is not possible yet. Flagged for the backend team.
 */
const logs = useAsync(wasteLogsAdminApi.findAll, [])

const sorted = computed(() =>
  [...(logs.data.value ?? [])].sort((a, b) => new Date(b.scannedAt) - new Date(a.scannedAt)),
)

onMounted(logs.run)
</script>

<template>
  <div class="stack">
    <PageHeader eyebrow="Администрирование" title="Журнал сдачи отходов" />

    <StateBlock
      :loading="logs.loading.value"
      :error="logs.error.value ?? ''"
      :empty="sorted.length === 0"
      empty-title="Записей нет"
      empty-text="Журнал заполняется после первой сдачи отходов."
      @retry="logs.run"
    >
      <div class="card table-wrap">
        <table>
          <thead>
            <tr>
              <th>Когда</th>
              <th class="num">Пользователь</th>
              <th class="num">Контейнер</th>
              <th class="num">EcoCoins</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in sorted" :key="log.id">
              <td>{{ formatDateTime(log.scannedAt) }}</td>
              <td class="metric num">#{{ log.userId }}</td>
              <td class="metric num">#{{ log.ecoPointContainerId }}</td>
              <td class="metric num coins">+{{ formatNumber(log.ecoCoinsEarned) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <p class="text-muted hint">
        Вес отходов и прирост заполненности пока не сохраняются в журнале.
      </p>
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

.coins {
  color: var(--c-coin);
  font-weight: 600;
}

.hint {
  font-size: var(--text-sm);
}
</style>
