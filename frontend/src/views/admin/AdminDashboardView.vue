<script setup>
import { onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin'
import { containersAdminApi } from '@/api/ecoWaste'
import { useAsync } from '@/composables/useAsync'
import { useEcoContainerSocket } from '@/composables/useEcoContainerSocket'
import { formatNumber } from '@/utils/format'
import { WASTE_TYPE_LABELS } from '@/utils/constants'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'
import FullnessBar from '@/components/common/FullnessBar.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const tab = ref('overview')

const dashboard = useAsync(adminApi.dashboard)
const containers = useAsync(containersAdminApi.findAll, [])
const vacancies = useAsync(() => adminApi.vacancies(0, 20))

const alerts = ref([])
const toggling = ref(null)

// Containers past 90% publish to /topic/admin/alerts.
useEcoContainerSocket({
  onAlert(message) {
    alerts.value = [{ id: Date.now(), message }, ...alerts.value].slice(0, 5)
  },
  onContainer(updated) {
    const list = containers.data.value ?? []
    containers.data.value = list.map((c) => (c.id === updated.id ? updated : c))
  },
})

async function toggleVacancy(vacancy) {
  toggling.value = vacancy.id
  try {
    await adminApi.setVacancyStatus(vacancy.id, !vacancy.isActive)
    await vacancies.run()
  } finally {
    toggling.value = null
  }
}

function switchTab(next) {
  tab.value = next
  if (next === 'containers' && !containers.data.value?.length) containers.run()
  if (next === 'vacancies' && !vacancies.data.value) vacancies.run()
}

onMounted(dashboard.run)

const metrics = [
  { key: 'totalUsers', label: 'Пользователи' },
  { key: 'students', label: 'Студенты' },
  { key: 'employees', label: 'Сотрудники' },
  { key: 'hrManagers', label: 'HR-менеджеры' },
  { key: 'totalContainers', label: 'Контейнеры' },
  { key: 'activeContainers', label: 'Активные контейнеры' },
  { key: 'criticalContainers', label: 'Требуют вывоза' },
  { key: 'totalCompanies', label: 'Компании' },
  { key: 'partnerCompanies', label: 'Партнёры' },
  { key: 'totalVacancies', label: 'Вакансии' },
  { key: 'activeVacancies', label: 'Активные вакансии' },
  { key: 'totalApplications', label: 'Отклики' },
  { key: 'totalEcoTransactions', label: 'Начисления' },
]
</script>

<template>
  <div class="stack">
    <PageHeader eyebrow="Администрирование" title="Панель управления" />

    <div v-if="alerts.length" class="alerts">
      <p class="eyebrow">Контейнеры заполнены</p>
      <ul>
        <li v-for="alert in alerts" :key="alert.id">{{ alert.message }}</li>
      </ul>
    </div>

    <nav class="tabs">
      <button :class="{ active: tab === 'overview' }" type="button" @click="switchTab('overview')">
        Обзор
      </button>
      <button :class="{ active: tab === 'containers' }" type="button" @click="switchTab('containers')">
        Контейнеры
      </button>
      <button :class="{ active: tab === 'vacancies' }" type="button" @click="switchTab('vacancies')">
        Вакансии
      </button>
    </nav>

    <StateBlock
      v-if="tab === 'overview'"
      :loading="dashboard.loading.value"
      :error="dashboard.error.value ?? ''"
      @retry="dashboard.run"
    >
      <ul class="metrics">
        <li v-for="metric in metrics" :key="metric.key" class="card metric-card">
          <p class="text-muted">{{ metric.label }}</p>
          <p
            :class="[
              'metric',
              'metric-card__value',
              { 'metric-card__value--alert': metric.key === 'criticalContainers' && dashboard.data.value?.[metric.key] > 0 },
            ]"
          >
            {{ formatNumber(dashboard.data.value?.[metric.key]) }}
          </p>
        </li>
      </ul>
    </StateBlock>

    <StateBlock
      v-else-if="tab === 'containers'"
      :loading="containers.loading.value"
      :error="containers.error.value ?? ''"
      :empty="(containers.data.value ?? []).length === 0"
      empty-title="Контейнеров нет"
      @retry="containers.run"
    >
      <ul class="list">
        <li v-for="c in containers.data.value" :key="c.id" class="card bin">
          <div class="bin__info">
            <h3>{{ c.title }}</h3>
            <p class="text-muted">
              {{ WASTE_TYPE_LABELS[c.wasteType] }} ·
              {{ c.isActive ? 'активен' : 'отключён' }}
            </p>
          </div>
          <FullnessBar :value="c.fullnessPercentage" />
        </li>
      </ul>
    </StateBlock>

    <StateBlock
      v-else
      :loading="vacancies.loading.value"
      :error="vacancies.error.value ?? ''"
      :empty="(vacancies.data.value?.content ?? []).length === 0"
      empty-title="Вакансий нет"
      @retry="vacancies.run"
    >
      <ul class="list">
        <li v-for="v in vacancies.data.value.content" :key="v.id" class="card vacancy">
          <div>
            <h3>{{ v.title }}</h3>
            <p class="text-muted">{{ v.companyName }}</p>
          </div>
          <BaseButton
            :variant="v.isActive ? 'danger' : 'primary'"
            :loading="toggling === v.id"
            @click="toggleVacancy(v)"
          >
            {{ v.isActive ? 'Снять с публикации' : 'Опубликовать' }}
          </BaseButton>
        </li>
      </ul>
    </StateBlock>
  </div>
</template>

<style scoped>
.alerts {
  padding: var(--space-4);
  background: var(--c-danger-soft);
  border: 1px solid var(--c-danger);
  border-radius: var(--radius);
  color: var(--c-danger);
}

.alerts .eyebrow {
  color: var(--c-danger);
}

.alerts ul {
  margin-top: var(--space-2);
  padding-left: var(--space-4);
  font-size: var(--text-sm);
}

.tabs {
  display: flex;
  gap: var(--space-2);
  border-bottom: 1px solid var(--c-line);
}

.tabs button {
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

.metrics {
  list-style: none;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: var(--space-3);
}

.metric-card {
  padding: var(--space-4);
}

.metric-card__value {
  font-size: var(--text-xl);
  font-weight: 600;
}

.metric-card__value--alert {
  color: var(--c-danger);
}

.list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.bin,
.vacancy {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-5);
  padding: var(--space-4);
}

.bin__info {
  min-width: 200px;
}

h3 {
  font-size: var(--text-base);
}
</style>
