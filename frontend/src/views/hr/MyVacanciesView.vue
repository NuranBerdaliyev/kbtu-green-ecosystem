<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { vacanciesApi, companiesApi } from '@/api/career'
import { useAsync } from '@/composables/useAsync'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseTextarea from '@/components/common/BaseTextarea.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const vacancies = useAsync(vacanciesApi.myVacancies, [])
const companies = useAsync(companiesApi.myCompanies, [])

const editingId = ref(null)
const creating = ref(false)
const saving = ref(false)
const error = ref('')
const fieldErrors = ref({})

const form = reactive({ companyId: null, title: '', description: '' })

const isFormOpen = computed(() => creating.value || editingId.value !== null)

/** VacancyService refuses to publish for a company without partner status. */
const partnerCompanies = computed(() => (companies.data.value ?? []).filter((c) => c.isPartner))
const hasPartnerCompany = computed(() => partnerCompanies.value.length > 0)

function openCreate() {
  Object.assign(form, {
    companyId: partnerCompanies.value[0]?.id ?? null,
    title: '',
    description: '',
  })
  editingId.value = null
  creating.value = true
  error.value = ''
  fieldErrors.value = {}
}

function openEdit(vacancy) {
  Object.assign(form, {
    companyId: vacancy.companyId ?? partnerCompanies.value[0]?.id ?? null,
    title: vacancy.title ?? '',
    description: vacancy.description ?? '',
  })
  creating.value = false
  editingId.value = vacancy.id
  error.value = ''
  fieldErrors.value = {}
}

function close() {
  creating.value = false
  editingId.value = null
}

async function save() {
  saving.value = true
  error.value = ''
  fieldErrors.value = {}
  try {
    const payload = {
      companyId: form.companyId,
      title: form.title,
      description: form.description,
    }
    if (editingId.value) await vacanciesApi.update(editingId.value, payload)
    else await vacanciesApi.create(payload)
    await vacancies.run()
    close()
  } catch (e) {
    error.value = e.message
    fieldErrors.value = e.fieldErrors ?? {}
  } finally {
    saving.value = false
  }
}

async function remove(vacancy) {
  if (!window.confirm(`Удалить вакансию «${vacancy.title}»?`)) return
  try {
    await vacanciesApi.remove(vacancy.id)
    await vacancies.run()
  } catch (e) {
    error.value = e.message
  }
}

const canSave = computed(
  () => form.companyId && form.title.trim().length > 0 && form.description.trim().length > 0,
)

onMounted(() => {
  vacancies.run()
  companies.run()
})
</script>

<template>
  <div class="stack">
    <PageHeader eyebrow="HR" title="Мои вакансии">
      <template #actions>
        <BaseButton v-if="!isFormOpen && hasPartnerCompany" @click="openCreate">
          Новая вакансия
        </BaseButton>
      </template>
    </PageHeader>

    <p v-if="!companies.loading.value && !hasPartnerCompany" class="card notice">
      Публиковать вакансии может только компания-партнёр.
      <RouterLink :to="{ name: 'my-company' }" class="link">Перейти к компаниям</RouterLink>
    </p>

    <section v-if="isFormOpen" class="card stack">
      <h2>{{ editingId ? 'Редактирование' : 'Новая вакансия' }}</h2>

      <label class="field">
        Компания
        <select v-model.number="form.companyId">
          <option v-for="c in partnerCompanies" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
        <span v-if="fieldErrors.companyId" class="field__error">{{ fieldErrors.companyId }}</span>
      </label>

      <BaseInput v-model="form.title" label="Название позиции" :error="fieldErrors.title" />
      <BaseTextarea
        v-model="form.description"
        label="Описание"
        :rows="6"
        :error="fieldErrors.description"
      />

      <p v-if="error" class="error">{{ error }}</p>
      <div class="row">
        <BaseButton :loading="saving" :disabled="!canSave" @click="save">Сохранить</BaseButton>
        <BaseButton variant="ghost" @click="close">Отмена</BaseButton>
      </div>
    </section>

    <StateBlock
      :loading="vacancies.loading.value"
      :error="vacancies.error.value ?? ''"
      :empty="(vacancies.data.value ?? []).length === 0"
      empty-title="Вакансий пока нет"
      @retry="vacancies.run"
    >
      <ul class="list">
        <li v-for="vacancy in vacancies.data.value" :key="vacancy.id" class="card vacancy">
          <div class="vacancy__info">
            <div class="vacancy__top">
              <h3>{{ vacancy.title }}</h3>
              <span :class="['status', vacancy.isActive ? 'status--live' : 'status--off']">
                {{ vacancy.isActive ? 'Опубликована' : 'Снята' }}
              </span>
            </div>
            <p class="text-muted">{{ vacancy.companyName }}</p>
          </div>
          <div class="row">
            <!--
              GET /career/vacancies/{id} only resolves active vacancies from
              partner companies, so this link would 404 for a withdrawn one.
            -->
            <RouterLink
              v-if="vacancy.isActive"
              :to="{ name: 'vacancy-details', params: { id: vacancy.id } }"
            >
              <BaseButton variant="ghost">Кандидаты</BaseButton>
            </RouterLink>
            <span v-else class="text-muted withdrawn">Снята — кандидаты недоступны</span>
            <BaseButton variant="ghost" @click="openEdit(vacancy)">Изменить</BaseButton>
            <BaseButton variant="danger" @click="remove(vacancy)">Удалить</BaseButton>
          </div>
        </li>
      </ul>
    </StateBlock>
  </div>
</template>

<style scoped>
h2 {
  font-size: var(--text-lg);
}

.notice {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  background: var(--c-coin-soft);
  border-color: var(--c-coin);
  color: var(--c-coin);
  font-size: var(--text-sm);
}

.link {
  font-weight: 600;
  text-decoration: underline;
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
  justify-content: space-between;
  align-items: center;
  gap: var(--space-5);
  flex-wrap: wrap;
}

.vacancy__info {
  flex: 1;
  min-width: 220px;
}

.vacancy__top {
  display: flex;
  align-items: center;
  gap: var(--space-3);
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

.status--live {
  background: var(--c-moss-soft);
  color: var(--c-moss-dark);
}

.status--off {
  background: var(--c-surface-sunk);
  color: var(--c-ink-muted);
}

.withdrawn {
  font-size: var(--text-sm);
  align-self: center;
}

.field {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--c-ink-soft);
}

.field select {
  padding: var(--space-3);
  border: 1px solid var(--c-line-strong);
  border-radius: var(--radius-sm);
  background: var(--c-surface);
}

.field__error,
.error {
  color: var(--c-danger);
  font-size: var(--text-sm);
}

.error {
  padding: var(--space-3);
  background: var(--c-danger-soft);
  border-radius: var(--radius-sm);
}
</style>
