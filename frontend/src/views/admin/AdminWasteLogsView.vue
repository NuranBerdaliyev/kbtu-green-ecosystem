<script setup>
import { computed, onMounted, ref } from 'vue'
import { wasteLogsAdminApi } from '@/api/ecoWaste'
import { useAsync } from '@/composables/useAsync'
import { formatNumber, formatDateTime } from '@/utils/format'
import { WASTE_TYPE_LABELS, DEPOSIT_STATUS } from '@/utils/constants'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import BaseButton from '@/components/common/BaseButton.vue'

/**
 * Moderation queue. Approving applies the weight to the container, recomputes
 * fullness, grants EcoCoins/ESG/CO2 and broadcasts over WebSocket — all
 * atomically on the backend. Rejecting changes nothing but the status.
 */
const tab = ref('pending')

const pending = useAsync(wasteLogsAdminApi.findPending, [])
const all = useAsync(wasteLogsAdminApi.findAll, [])

const acting = ref(null)
const error = ref('')

const active = computed(() => (tab.value === 'pending' ? pending : all))

const rows = computed(() => {
  const list = tab.value === 'pending' ? (pending.data.value ?? []) : (all.data.value ?? [])
  return [...list].sort((a, b) => new Date(b.scannedAt) - new Date(a.scannedAt))
})

async function decide(log, approve) {
  acting.value = log.id
  error.value = ''
  try {
    if (approve) await wasteLogsAdminApi.approve(log.id)
    else await wasteLogsAdminApi.reject(log.id)
    // Both lists change: the item leaves the queue and gains a decision.
    await Promise.all([pending.run(), tab.value === 'all' ? all.run() : Promise.resolve()])
  } catch (e) {
    error.value = e.message
  } finally {
    acting.value = null
  }
}

function switchTab(next) {
  tab.value = next
  if (next === 'all' && !all.data.value?.length) all.run()
}

onMounted(pending.run)
</script>

<template>
  <div class="stack">
    <PageHeader
      eyebrow="Администрирование"
      title="Сдача отходов"
      subtitle="Заявки студентов проверяются вручную: до подтверждения награда не начисляется."
    />

    <nav class="tabs">
      <button :class="{ active: tab === 'pending' }" type="button" @click="switchTab('pending')">
        На проверке
        <span v-if="(pending.data.value ?? []).length" class="count">
          {{ pending.data.value.length }}
        </span>
      </button>
      <button :class="{ active: tab === 'all' }" type="button" @click="switchTab('all')">
        Вся история
      </button>
    </nav>

    <p v-if="error" class="error">{{ error }}</p>

    <StateBlock
      :loading="active.loading.value"
      :error="active.error.value ?? ''"
      :empty="rows.length === 0"
      :empty-title="tab === 'pending' ? 'Нет заявок на проверке' : 'Записей нет'"
      empty-text="Заявки появляются здесь сразу после сдачи отходов."
      @retry="active.run"
    >
      <ul class="list">
        <li v-for="log in rows" :key="log.id" class="card log">
          <div class="log__main">
            <div class="log__top">
              <h3>
                {{ formatNumber(log.wasteWeightGrams) }} г ·
                {{ WASTE_TYPE_LABELS[log.wasteType] ?? log.wasteType }}
              </h3>
              <StatusBadge :status="log.status" kind="deposit" />
            </div>

            <dl class="log__facts">
              <div>
                <dt>Пользователь</dt>
                <dd class="metric">#{{ log.userId }}</dd>
              </div>
              <div>
                <dt>Контейнер</dt>
                <dd class="metric">#{{ log.ecoPointContainerId }}</dd>
              </div>
              <div>
                <dt>Сдано</dt>
                <dd>{{ formatDateTime(log.scannedAt) }}</dd>
              </div>
              <div v-if="log.status !== DEPOSIT_STATUS.PENDING">
                <dt>EcoCoins</dt>
                <dd class="metric coins">+{{ formatNumber(log.ecoCoinsEarned) }}</dd>
              </div>
              <div v-if="log.fullnessDeltaPercentage">
                <dt>Заполненность</dt>
                <dd class="metric">+{{ log.fullnessDeltaPercentage }}%</dd>
              </div>
              <div v-if="log.reviewedAt">
                <dt>Проверено</dt>
                <dd>{{ formatDateTime(log.reviewedAt) }} · #{{ log.reviewedById }}</dd>
              </div>
            </dl>
          </div>

          <div v-if="log.status === DEPOSIT_STATUS.PENDING" class="log__actions">
            <BaseButton :loading="acting === log.id" @click="decide(log, true)">
              Подтвердить
            </BaseButton>
            <BaseButton variant="danger" :loading="acting === log.id" @click="decide(log, false)">
              Отклонить
            </BaseButton>
          </div>
        </li>
      </ul>
    </StateBlock>
  </div>
</template>

<style scoped>
.tabs {
  display: flex;
  gap: var(--space-2);
  border-bottom: 1px solid var(--c-line);
}

.tabs button {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border: none;
  background: none;
  border-bottom: 2px solid transparent;
  color: var(--c-ink-muted);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
}

.tabs button.active {
  color: var(--c-moss);
  border-bottom-color: var(--c-moss);
}

.count {
  padding: 0 var(--space-2);
  background: var(--c-coin);
  color: #fff;
  border-radius: 999px;
  font-size: var(--text-xs);
}

.list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.log {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-5);
  flex-wrap: wrap;
}

.log__main {
  flex: 1;
  min-width: 260px;
}

.log__top {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

h3 {
  font-size: var(--text-base);
}

.log__facts {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4) var(--space-5);
}

dt {
  font-size: var(--text-xs);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--c-ink-muted);
}

dd {
  font-size: var(--text-sm);
  font-weight: 600;
}

.coins {
  color: var(--c-coin);
}

.log__actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.error {
  padding: var(--space-3);
  background: var(--c-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--c-danger);
  font-size: var(--text-sm);
}
</style>
