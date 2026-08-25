<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { companiesApi } from '@/api/career'
import { useAsync } from '@/composables/useAsync'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseTextarea from '@/components/common/BaseTextarea.vue'
import BaseButton from '@/components/common/BaseButton.vue'

/**
 * HR manages their own companies here. Partner status is deliberately absent:
 * only an ADMIN can grant it (CompanyService rejects self-promotion).
 */
const companies = useAsync(companiesApi.myCompanies, [])

const editingId = ref(null)
const creating = ref(false)
const saving = ref(false)
const error = ref('')
const fieldErrors = ref({})

const form = reactive({ name: '', description: '', website: '' })

const isFormOpen = computed(() => creating.value || editingId.value !== null)

function openCreate() {
  Object.assign(form, { name: '', description: '', website: '' })
  editingId.value = null
  creating.value = true
  error.value = ''
  fieldErrors.value = {}
}

function openEdit(company) {
  Object.assign(form, {
    name: company.name ?? '',
    description: company.description ?? '',
    website: company.website ?? '',
  })
  creating.value = false
  editingId.value = company.id
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
      name: form.name,
      description: form.description || null,
      website: form.website || null,
    }
    if (editingId.value) await companiesApi.update(editingId.value, payload)
    else await companiesApi.create(payload)
    await companies.run()
    close()
  } catch (e) {
    error.value = e.message
    fieldErrors.value = e.fieldErrors ?? {}
  } finally {
    saving.value = false
  }
}

/**
 * CompanyService refuses to delete a company that still has vacancies, so the
 * old warning ("its vacancies will disappear") was wrong in both directions.
 */
async function remove(company) {
  if (!window.confirm(`Удалить «${company.name}»?`)) return
  try {
    await companiesApi.remove(company.id)
    await companies.run()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(companies.run)
</script>

<template>
  <div class="stack">
    <PageHeader
      eyebrow="HR"
      title="Мои компании"
      subtitle="Публиковать вакансии может только компания со статусом партнёра."
    >
      <template #actions>
        <BaseButton v-if="!isFormOpen" @click="openCreate">Добавить компанию</BaseButton>
      </template>
    </PageHeader>

    <section v-if="isFormOpen" class="card stack">
      <h2>{{ editingId ? 'Редактирование' : 'Новая компания' }}</h2>
      <BaseInput v-model="form.name" label="Название" :error="fieldErrors.name" />
      <BaseInput
        v-model="form.website"
        label="Сайт"
        placeholder="https://example.com"
        :error="fieldErrors.website"
      />
      <BaseTextarea
        v-model="form.description"
        label="Описание"
        :rows="4"
        :error="fieldErrors.description"
      />
      <p v-if="error" class="error">{{ error }}</p>
      <div class="row">
        <BaseButton :loading="saving" :disabled="!form.name.trim()" @click="save">
          Сохранить
        </BaseButton>
        <BaseButton variant="ghost" @click="close">Отмена</BaseButton>
      </div>
    </section>

    <StateBlock
      :loading="companies.loading.value"
      :error="companies.error.value ?? ''"
      :empty="(companies.data.value ?? []).length === 0"
      empty-title="Компаний пока нет"
      empty-text="Добавьте компанию, чтобы публиковать вакансии."
      @retry="companies.run"
    >
      <ul class="list">
        <li v-for="company in companies.data.value" :key="company.id" class="card company">
          <div class="company__info">
            <div class="company__top">
              <h3>{{ company.name }}</h3>
              <span :class="['status', company.isPartner ? 'status--partner' : 'status--pending']">
                {{ company.isPartner ? 'Партнёр' : 'Ожидает подтверждения' }}
              </span>
            </div>
            <p v-if="company.description" class="text-muted">{{ company.description }}</p>
            <p v-if="!company.isPartner" class="text-muted hint">
              Статус партнёра назначает администратор. До этого вакансии не публикуются.
            </p>
            <p class="text-muted hint">
              Удалить компанию можно только после удаления всех её вакансий.
            </p>
          </div>
          <div class="row">
            <BaseButton variant="ghost" @click="openEdit(company)">Изменить</BaseButton>
            <BaseButton variant="danger" @click="remove(company)">Удалить</BaseButton>
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
  align-items: flex-start;
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

.hint {
  font-size: var(--text-sm);
  margin-top: var(--space-2);
}

.error {
  padding: var(--space-3);
  background: var(--c-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--c-danger);
  font-size: var(--text-sm);
}
</style>
