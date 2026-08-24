<script setup>
import { onMounted, ref } from 'vue'
import { companiesApi } from '@/api/career'
import { useAsync } from '@/composables/useAsync'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'
import BaseButton from '@/components/common/BaseButton.vue'

/** Partner status is admin-only — HR cannot grant it to their own company. */
const companies = useAsync(companiesApi.findAll, [])
const saving = ref(null)
const error = ref('')

async function togglePartner(company) {
  saving.value = company.id
  error.value = ''
  try {
    await companiesApi.setPartnerStatus(company.id, !company.isPartner)
    await companies.run()
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = null
  }
}

onMounted(companies.run)
</script>

<template>
  <div class="stack">
    <PageHeader
      eyebrow="Администрирование"
      title="Компании"
      subtitle="Вакансии публикуются только от компаний со статусом партнёра."
    />

    <p v-if="error" class="error">{{ error }}</p>

    <StateBlock
      :loading="companies.loading.value"
      :error="companies.error.value ?? ''"
      :empty="(companies.data.value ?? []).length === 0"
      empty-title="Компаний нет"
      @retry="companies.run"
    >
      <ul class="list">
        <li v-for="company in companies.data.value" :key="company.id" class="card company">
          <div class="company__info">
            <div class="company__top">
              <h3>{{ company.name }}</h3>
              <span :class="['status', company.isPartner ? 'status--partner' : 'status--pending']">
                {{ company.isPartner ? 'Партнёр' : 'Не подтверждена' }}
              </span>
            </div>
            <p v-if="company.description" class="text-muted">{{ company.description }}</p>
            <a
              v-if="company.website"
              :href="company.website"
              target="_blank"
              rel="noopener"
              class="link"
            >
              {{ company.website }}
            </a>
          </div>
          <BaseButton
            :variant="company.isPartner ? 'danger' : 'primary'"
            :loading="saving === company.id"
            @click="togglePartner(company)"
          >
            {{ company.isPartner ? 'Снять партнёрство' : 'Подтвердить партнёра' }}
          </BaseButton>
        </li>
      </ul>
    </StateBlock>
  </div>
</template>

<style scoped>
.list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.company {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-5);
  flex-wrap: wrap;
}

.company__info {
  flex: 1;
  min-width: 240px;
}

.company__top {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}

h3 {
  font-size: var(--text-lg);
}

.status {
  padding: 2px var(--space-2);
  border-radius: 999px;
  font-size: var(--text-xs);
  font-weight: 600;
  white-space: nowrap;
}

.status--partner {
  background: var(--c-moss-soft);
  color: var(--c-moss-dark);
}

.status--pending {
  background: var(--c-coin-soft);
  color: var(--c-coin);
}

.link {
  font-size: var(--text-sm);
  color: var(--c-moss);
  font-weight: 600;
}

.error {
  padding: var(--space-3);
  background: var(--c-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--c-danger);
  font-size: var(--text-sm);
}
</style>
