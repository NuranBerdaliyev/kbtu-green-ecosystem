<script setup>
import { onMounted, reactive } from 'vue'
import { RouterLink } from 'vue-router'
import { vacanciesApi } from '@/api/career'
import { useAsync } from '@/composables/useAsync'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'

/**
 * There is no partnerOnly filter: VacancyService already returns only
 * vacancies from partner companies, so the toggle would change nothing.
 */
const filters = reactive({ query: '', page: 0 })

const vacancies = useAsync(() => {
  const params = { page: filters.page, size: 20 }
  if (filters.query.trim()) params.query = filters.query.trim()
  return vacanciesApi.search(params)
})

let debounce
function onSearch() {
  clearTimeout(debounce)
  debounce = setTimeout(() => {
    filters.page = 0
    vacancies.run()
  }, 300)
}

onMounted(vacancies.run)
</script>

<template>
  <div class="stack">
    <PageHeader
      eyebrow="Career Hub"
      title="Вакансии"
      subtitle="Партнёры видят ваш ESG-рейтинг при отборе кандидатов."
    />

    <div class="card filters">
      <input
        v-model="filters.query"
        type="search"
        placeholder="Поиск по названию"
        @input="onSearch"
      />
      <p class="text-muted check">Показаны вакансии компаний-партнёров.</p>
    </div>

    <StateBlock
      :loading="vacancies.loading.value"
      :error="vacancies.error.value ?? ''"
      :empty="(vacancies.data.value?.content ?? []).length === 0"
      empty-title="Вакансий не найдено"
      empty-text="Попробуйте изменить поисковый запрос."
      @retry="vacancies.run"
    >
      <ul class="list">
        <li v-for="vacancy in vacancies.data.value.content" :key="vacancy.id">
          <RouterLink
            :to="{ name: 'vacancy-details', params: { id: vacancy.id } }"
            class="card vacancy"
          >
            <div class="vacancy__top">
              <div>
                <h3>{{ vacancy.title }}</h3>
                <p class="text-muted">{{ vacancy.companyName }}</p>
              </div>
              <span v-if="vacancy.partnerCompany" class="partner">Партнёр</span>
            </div>
            <p class="vacancy__desc">{{ vacancy.description }}</p>
          </RouterLink>
        </li>
      </ul>

      <nav v-if="vacancies.data.value.totalPages > 1" class="pager">
        <button
          type="button"
          :disabled="filters.page === 0"
          @click="
            () => {
              filters.page -= 1
              vacancies.run()
            }
          "
        >
          Назад
        </button>
        <span class="metric"> {{ filters.page + 1 }} / {{ vacancies.data.value.totalPages }} </span>
        <button
          type="button"
          :disabled="filters.page + 1 >= vacancies.data.value.totalPages"
          @click="
            () => {
              filters.page += 1
              vacancies.run()
            }
          "
        >
          Вперёд
        </button>
      </nav>
    </StateBlock>
  </div>
</template>

<style scoped>
.filters {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  flex-wrap: wrap;
  padding: var(--space-4);
}

.filters input[type='search'] {
  flex: 1;
  min-width: 220px;
  padding: var(--space-3);
  border: 1px solid var(--c-line-strong);
  border-radius: var(--radius-sm);
}

.check {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--c-ink-soft);
  white-space: nowrap;
}

.list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.vacancy {
  display: block;
  transition: border-color 0.15s ease;
}

.vacancy:hover {
  border-color: var(--c-moss);
}

.vacancy__top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

.vacancy__desc {
  color: var(--c-ink-soft);
  font-size: var(--text-sm);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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

h3 {
  font-size: var(--text-lg);
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
