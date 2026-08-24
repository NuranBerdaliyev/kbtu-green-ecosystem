<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { profileApi } from '@/api/profile'
import { gamificationApi } from '@/api/gamification'
import { applicationsApi } from '@/api/career'
import { useAuthStore } from '@/stores/auth'
import { useAsync } from '@/composables/useAsync'
import { formatNumber, formatCo2, formatDelta, formatDateTime } from '@/utils/format'
import { ECO_SOURCE_LABELS, ESG_RATING_MAX, ROLE_LABELS } from '@/utils/constants'
import { can } from '@/utils/roles'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseTextarea from '@/components/common/BaseTextarea.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const auth = useAuthStore()

const history = useAsync(() => gamificationApi.history(0, 20))
const applications = useAsync(applicationsApi.myApplications, [])

/**
 * Registration does not create a Profile row, so GET /profiles/me returns 404
 * until the user saves once. That is an empty state, not an error.
 */
const profile = useAsync(async () => {
  try {
    return await profileApi.me()
  } catch (e) {
    if (e.status === 404) return null
    throw e
  }
})

/** Only students have job applications; other roles get a 403. */
const showsApplications = computed(() => can.applyToVacancy(auth.role))

const editing = ref(false)
const saving = ref(false)
const saveError = ref('')
const form = reactive({ phone: '', bio: '', birthDate: '', avatarUrl: '' })

async function startEdit() {
  editing.value = true
  const p = profile.data.value
  if (p)
    Object.assign(form, {
      phone: p.phone ?? '',
      bio: p.bio ?? '',
      birthDate: p.birthDate ?? '',
      avatarUrl: p.avatarUrl ?? '',
    })
}

async function save() {
  saving.value = true
  saveError.value = ''
  try {
    // ProfileRequestDto fields are all optional; send null instead of "".
    profile.data.value = await profileApi.updateMe({
      phone: form.phone || null,
      bio: form.bio || null,
      birthDate: form.birthDate || null,
      avatarUrl: form.avatarUrl || null,
    })
    editing.value = false
  } catch (e) {
    saveError.value = e.message
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  auth.loadStats()
  history.run()
  profile.run()
  if (showsApplications.value) applications.run()
})
</script>

<template>
  <div class="stack">
    <PageHeader :eyebrow="ROLE_LABELS[auth.role] ?? ''" :title="auth.fullName || 'Профиль'">
      <template #actions>
        <BaseButton v-if="!editing" variant="ghost" @click="startEdit">Редактировать</BaseButton>
      </template>
    </PageHeader>

    <section class="stats">
      <article class="card stat">
        <p class="text-muted">EcoCoins</p>
        <p class="metric stat__value">{{ formatNumber(auth.stats?.ecoCoinsBalance) }}</p>
      </article>
      <article class="card stat">
        <p class="text-muted">ESG-рейтинг</p>
        <p class="metric stat__value">
          {{ auth.stats?.esgRating ?? 0 }}<span class="stat__max">/{{ ESG_RATING_MAX }}</span>
        </p>
      </article>
      <article class="card stat">
        <p class="text-muted">Сокращено выбросов</p>
        <p class="metric stat__value">{{ formatCo2(auth.stats?.totalCo2Saved) }}</p>
      </article>
      <article class="card stat">
        <p class="text-muted">Место в рейтинге</p>
        <p class="metric stat__value">#{{ auth.stats?.leaderboardRank ?? '—' }}</p>
      </article>
    </section>

    <section v-if="editing" class="card stack">
      <h2>Личные данные</h2>
      <BaseInput v-model="form.phone" label="Телефон" />
      <BaseInput v-model="form.birthDate" label="Дата рождения" type="date" />
      <BaseInput v-model="form.avatarUrl" label="Ссылка на аватар" />
      <BaseTextarea v-model="form.bio" label="О себе" :max-length="500" :rows="4" />
      <p v-if="saveError" class="error">{{ saveError }}</p>
      <div class="row">
        <BaseButton :loading="saving" @click="save">Сохранить</BaseButton>
        <BaseButton variant="ghost" @click="editing = false">Отмена</BaseButton>
      </div>
    </section>

    <section v-else-if="profile.data.value?.bio" class="card">
      <p>{{ profile.data.value.bio }}</p>
    </section>

    <div class="columns">
      <section class="stack">
        <h2>История начислений</h2>
        <StateBlock
          :loading="history.loading.value"
          :error="history.error.value ?? ''"
          :empty="(history.data.value?.content ?? []).length === 0"
          empty-title="Активности пока нет"
          empty-text="Присоединитесь к поездке или сдайте отходы."
          @retry="history.run"
        >
          <ul class="list">
            <li v-for="tx in history.data.value.content" :key="tx.id" class="card tx">
              <div>
                <p class="tx__source">{{ ECO_SOURCE_LABELS[tx.source] ?? tx.source }}</p>
                <p class="text-muted tx__date">{{ formatDateTime(tx.createdAt) }}</p>
              </div>
              <div class="tx__deltas">
                <span class="metric tx__coins">{{ formatDelta(tx.ecoCoinsDelta) }}</span>
                <span class="text-muted">
                  ESG {{ formatDelta(tx.esgRatingDelta) }} · {{ formatCo2(tx.co2SavedDelta) }}
                </span>
              </div>
            </li>
          </ul>
        </StateBlock>
      </section>

      <section v-if="showsApplications" class="stack">
        <h2>Мои отклики</h2>
        <StateBlock
          :loading="applications.loading.value"
          :error="applications.error.value ?? ''"
          :empty="(applications.data.value ?? []).length === 0"
          empty-title="Откликов нет"
          empty-text="Загляните в раздел вакансий."
          @retry="applications.run"
        >
          <ul class="list">
            <li v-for="app in applications.data.value" :key="app.id" class="card app">
              <div>
                <p class="app__title">{{ app.vacancyTitle }}</p>
                <p class="text-muted">{{ app.companyName }}</p>
              </div>
              <StatusBadge :status="app.jobStatus" kind="job" />
            </li>
          </ul>
        </StateBlock>
      </section>
    </div>
  </div>
</template>

<style scoped>
.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: var(--space-4);
}

.stat__value {
  font-size: var(--text-2xl);
  font-weight: 600;
}

.stat__max {
  font-size: var(--text-lg);
  color: var(--c-ink-muted);
}

.columns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-5);
  align-items: start;
}

@media (max-width: 900px) {
  .columns {
    grid-template-columns: 1fr;
  }
}

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

.tx,
.app {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
}

.tx__source,
.app__title {
  font-weight: 600;
}

.tx__date {
  font-size: var(--text-sm);
}

.tx__deltas {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  font-size: var(--text-xs);
  text-align: right;
}

.tx__coins {
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--c-coin);
}

.error {
  padding: var(--space-3);
  background: var(--c-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--c-danger);
  font-size: var(--text-sm);
}
</style>
