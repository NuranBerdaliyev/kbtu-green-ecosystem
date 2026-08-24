<script setup>
import { onMounted } from 'vue'
import { companiesApi } from '@/api/career'
import { useAsync } from '@/composables/useAsync'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'

const companies = useAsync(companiesApi.findAll, [])

onMounted(companies.run)
</script>

<template>
  <div class="stack">
    <PageHeader
      eyebrow="Career Hub"
      title="Компании"
      subtitle="Партнёры университета публикуют вакансии прямо на платформе."
    />

    <StateBlock
      :loading="companies.loading.value"
      :error="companies.error.value ?? ''"
      :empty="(companies.data.value ?? []).length === 0"
      empty-title="Компаний пока нет"
      @retry="companies.run"
    >
      <ul class="grid">
        <li v-for="company in companies.data.value" :key="company.id" class="card company">
          <div class="company__top">
            <h3>{{ company.name }}</h3>
            <span v-if="company.isPartner" class="partner">Партнёр</span>
          </div>
          <p v-if="company.description" class="text-muted">{{ company.description }}</p>
          <a
            v-if="company.website"
            :href="company.website"
            target="_blank"
            rel="noopener"
            class="link"
          >
            Сайт компании
          </a>
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
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--space-4);
}

.company {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.company__top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-2);
}

h3 {
  font-size: var(--text-lg);
}

.partner {
  padding: 2px var(--space-2);
  background: var(--c-moss-soft);
  color: var(--c-moss-dark);
  border-radius: 999px;
  font-size: var(--text-xs);
  font-weight: 600;
  white-space: nowrap;
}

.link {
  margin-top: auto;
  color: var(--c-moss);
  font-size: var(--text-sm);
  font-weight: 600;
}
</style>
