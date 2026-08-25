<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { adminApi } from '@/api/admin'
import { useAsync } from '@/composables/useAsync'
import { useEcoContainerSocket } from '@/composables/useEcoContainerSocket'
import { formatNumber } from '@/utils/format'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const dashboard = useAsync(adminApi.dashboard)
const vacancies = useAsync(() => adminApi.vacancies(0, 20))

const alerts = ref([])
const toggling = ref(null)
const actionError = ref('')

/*
 * Alerts fire when a container crosses 90% from below — not on every change
 * above 90%. The payload is a string today and a JSON object in the newer
 * backend, so both shapes are rendered.
 */
const { connected } = useEcoContainerSocket({
  onAlert(payload) {
    const entry =
      typeof payload === 'string'
        ? { id: Date.now(), text: payload }
        : {
            id: `${payload.containerId}-${payload.crossedAt}`,
            text: `«${payload.title}» заполнен на ${payload.currentFullnessPercentage}%`,
            detail:
              payload.currentWeightGrams != null && payload.capacityGrams != null
                ? `${payload.currentWeightGrams} / ${payload.capacityGrams} г`
                : '',
          }
    alerts.value = [entry, ...alerts.value].slice(0, 5)
  },
})

async function toggleVacancy(vacancy) {
  toggling.value = vacancy.id
  actionError.value = ''
  try {
    await adminApi.setVacancyStatus(vacancy.id, !vacancy.isActive)
    await vacancies.run()
  } catch (e) {
    actionError.value = e.message
  } finally {
    toggling.value = null
  }
}

const sections = [
  { name: 'admin-containers', label: 'Контейнеры', text: 'Создание, правка, отметка о вывозе' },
  { name: 'admin-companies', label: 'Компании', text: 'Подтверждение партнёрства' },
  { name: 'admin-users', label: 'Пользователи', text: 'Назначение ролей' },
  { name: 'admin-waste-logs', label: 'Сдача отходов', text: 'Проверка заявок студентов' },
]

onMounted(() => {
  dashboard.run()
  vacancies.run()
})

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

    <p v-if="!connected" class="ws-state">
      Нет соединения с сервером обновлений — заполненность может быть неактуальной.
    </p>

    <div v-if="alerts.length" class="alerts">
      <p class="eyebrow">Контейнеры требуют вывоза</p>
      <ul>
        <li v-for="alert in alerts" :key="alert.id">
          {{ alert.text }}
          <span v-if="alert.detail" class="metric alerts__detail">{{ alert.detail }}</span>
        </li>
      </ul>
    </div>

    <nav class="sections">
      <RouterLink
        v-for="section in sections"
        :key="section.name"
        :to="{ name: section.name }"
        class="card section"
      >
        <h3>{{ section.label }}</h3>
        <p class="text-muted">{{ section.text }}</p>
      </RouterLink>
    </nav>

    <StateBlock
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
              {
                'metric-card__value--alert':
                  metric.key === 'criticalContainers' && dashboard.data.value?.[metric.key] > 0,
              },
            ]"
          >
            {{ formatNumber(dashboard.data.value?.[metric.key]) }}
          </p>
        </li>
      </ul>
    </StateBlock>

    <section class="stack">
      <h2>Модерация вакансий</h2>
      <p v-if="actionError" class="error">{{ actionError }}</p>
      <StateBlock
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
    </section>
  </div>
</template>

<style scoped>
.ws-state {
  padding: var(--space-3);
  background: var(--c-surface-sunk);
  border-radius: var(--radius-sm);
  color: var(--c-ink-muted);
  font-size: var(--text-sm);
}

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

.alerts__detail {
  margin-left: var(--space-2);
  opacity: 0.8;
}

.sections {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-3);
}

.section {
  padding: var(--space-4);
  transition:
    border-color 0.15s ease,
    transform 0.15s ease;
}

.section:hover {
  border-color: var(--c-moss);
  transform: translateY(-2px);
}

h2 {
  font-size: var(--text-lg);
}

.error {
  padding: var(--space-3);
  background: var(--c-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--c-danger);
  font-size: var(--text-sm);
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

.vacancy {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-5);
  padding: var(--space-4);
}

h3 {
  font-size: var(--text-base);
}
</style>
