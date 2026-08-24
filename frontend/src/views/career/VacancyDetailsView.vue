<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { vacanciesApi, applicationsApi } from '@/api/career'
import { useAuthStore } from '@/stores/auth'
import { useAsync } from '@/composables/useAsync'
import { can } from '@/utils/roles'
import {
  COVER_LETTER_MIN,
  COVER_LETTER_MAX,
  CANDIDATE_SORT,
  JOB_STATUS_LABELS,
  JOB_STATUS_TRANSITIONS,
} from '@/utils/constants'
import { formatNumber, formatCo2, formatDateTime } from '@/utils/format'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BaseTextarea from '@/components/common/BaseTextarea.vue'

const route = useRoute()
const auth = useAuthStore()
const vacancyId = Number(route.params.id)

const vacancy = useAsync(() => vacanciesApi.findById(vacancyId))

/** Only students may apply; the backend rejects everyone else with 403. */
const canApplyByRole = computed(() => can.applyToVacancy(auth.role))
/** Only the vacancy owner may list candidates, so check ownership, not just role. */
const ownsVacancy = computed(
  () => can.reviewCandidates(auth.role) && vacancy.data.value?.hrManagerId === auth.userId,
)

// ---- student side ----------------------------------------------------------
const myApplications = useAsync(applicationsApi.myApplications, [])
const coverLetter = ref('')
const applying = ref(false)
const applyError = ref('')

/** Survives a page reload, unlike a local "submitted" flag. */
const existingApplication = computed(() =>
  (myApplications.data.value ?? []).find((a) => a.vacancyId === vacancyId),
)

const canSubmit = computed(
  () =>
    coverLetter.value.length >= COVER_LETTER_MIN && coverLetter.value.length <= COVER_LETTER_MAX,
)

async function apply() {
  if (!canSubmit.value) return
  applying.value = true
  applyError.value = ''
  try {
    await applicationsApi.apply(vacancyId, coverLetter.value)
    await myApplications.run()
    coverLetter.value = ''
  } catch (e) {
    applyError.value = e.message
  } finally {
    applying.value = false
  }
}

// ---- HR side ---------------------------------------------------------------
const sort = ref(CANDIDATE_SORT.ESG_DESC)
const candidates = useAsync(() => applicationsApi.candidates(vacancyId, sort.value), [])
const changing = ref(null)
const statusError = ref('')

/** The backend allows PENDING -> REVIEWED -> ACCEPTED|REJECTED and nothing else. */
const nextStatuses = (status) => JOB_STATUS_TRANSITIONS[status] ?? []

async function setStatus(applicationId, status) {
  changing.value = applicationId
  statusError.value = ''
  try {
    await applicationsApi.changeStatus(applicationId, status)
    await candidates.run()
  } catch (e) {
    statusError.value = e.message
  } finally {
    changing.value = null
  }
}

onMounted(async () => {
  await vacancy.run()
  if (ownsVacancy.value) candidates.run()
  if (canApplyByRole.value) myApplications.run()
})
</script>

<template>
  <div class="stack">
    <StateBlock
      :loading="vacancy.loading.value"
      :error="vacancy.error.value ?? ''"
      :skeletons="2"
      @retry="vacancy.run"
    >
      <template v-if="vacancy.data.value">
        <PageHeader
          :eyebrow="vacancy.data.value.companyName"
          :title="vacancy.data.value.title"
          :subtitle="vacancy.data.value.partnerCompany ? 'Компания-партнёр университета' : ''"
        />

        <div class="layout">
          <article class="card description">
            <h2>Описание</h2>
            <p>{{ vacancy.data.value.description }}</p>
          </article>

          <aside v-if="canApplyByRole" class="card stack">
            <template v-if="existingApplication">
              <p class="eyebrow">Вы откликнулись</p>
              <div class="row">
                <StatusBadge :status="existingApplication.jobStatus" kind="job" />
                <span class="text-muted">{{ formatDateTime(existingApplication.appliedAt) }}</span>
              </div>
              <p class="text-muted">
                Статус изменится, когда HR рассмотрит отклик. Повторно откликнуться нельзя.
              </p>
            </template>
            <template v-else>
              <h2>Откликнуться</h2>
              <BaseTextarea
                v-model="coverLetter"
                label="Сопроводительное письмо"
                placeholder="Почему вам интересна эта позиция?"
                :min-length="COVER_LETTER_MIN"
                :max-length="COVER_LETTER_MAX"
              />
              <p v-if="applyError" class="error">{{ applyError }}</p>
              <BaseButton :loading="applying" :disabled="!canSubmit" @click="apply">
                Отправить отклик
              </BaseButton>
              <p class="text-muted hint">
                HR увидит ваш ESG-рейтинг, баланс EcoCoins и сокращённый CO₂.
              </p>
            </template>
          </aside>

          <aside v-else-if="!ownsVacancy" class="card">
            <p class="text-muted">Откликаться на вакансии могут только студенты.</p>
          </aside>
        </div>

        <section v-if="ownsVacancy" class="stack">
          <div class="candidates__head">
            <h2>Кандидаты</h2>
            <select v-model="sort" @change="candidates.run()">
              <option :value="CANDIDATE_SORT.ESG_DESC">Сначала с высоким ESG</option>
              <option :value="CANDIDATE_SORT.APPLIED_AT_DESC">Сначала новые</option>
            </select>
          </div>

          <p v-if="statusError" class="error">{{ statusError }}</p>

          <StateBlock
            :loading="candidates.loading.value"
            :error="candidates.error.value ?? ''"
            :empty="(candidates.data.value ?? []).length === 0"
            empty-title="Откликов пока нет"
            @retry="candidates.run"
          >
            <ul class="list">
              <li v-for="c in candidates.data.value" :key="c.applicationId" class="card candidate">
                <div class="candidate__top">
                  <div>
                    <h3>
                      {{ c.fullName }}
                      <span v-if="c.recommended" class="recommended">Рекомендован</span>
                    </h3>
                    <p class="text-muted">{{ formatDateTime(c.appliedAt) }}</p>
                  </div>
                  <StatusBadge :status="c.jobStatus" kind="job" />
                </div>

                <dl class="candidate__stats">
                  <div>
                    <dt>ESG</dt>
                    <dd class="metric">{{ c.esgRating }}</dd>
                  </div>
                  <div>
                    <dt>EcoCoins</dt>
                    <dd class="metric">{{ formatNumber(c.ecoCoinsBalance) }}</dd>
                  </div>
                  <div>
                    <dt>CO₂</dt>
                    <dd class="metric">{{ formatCo2(c.totalCo2Saved) }}</dd>
                  </div>
                </dl>

                <p class="candidate__letter">{{ c.coverLetter }}</p>

                <!-- Only transitions the backend actually allows are offered. -->
                <div v-if="nextStatuses(c.jobStatus).length" class="row">
                  <BaseButton
                    v-for="next in nextStatuses(c.jobStatus)"
                    :key="next"
                    :variant="
                      next === 'REJECTED' ? 'danger' : next === 'REVIEWED' ? 'ghost' : 'primary'
                    "
                    :loading="changing === c.applicationId"
                    @click="setStatus(c.applicationId, next)"
                  >
                    {{ JOB_STATUS_LABELS[next] }}
                  </BaseButton>
                </div>
                <p v-else class="text-muted hint">Решение принято, статус изменить нельзя.</p>
              </li>
            </ul>
          </StateBlock>
        </section>
      </template>
    </StateBlock>
  </div>
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: var(--space-5);
  align-items: start;
}

@media (max-width: 900px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

h2 {
  font-size: var(--text-lg);
  margin-bottom: var(--space-3);
}

.description p {
  white-space: pre-line;
  color: var(--c-ink-soft);
}

.candidates__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
}

.candidates__head h2 {
  margin: 0;
}

select {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--c-line-strong);
  border-radius: var(--radius-sm);
  background: var(--c-surface);
}

.list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.candidate__top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

h3 {
  font-size: var(--text-base);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.recommended {
  padding: 2px var(--space-2);
  background: var(--c-coin-soft);
  color: var(--c-coin);
  border-radius: 999px;
  font-size: var(--text-xs);
  font-weight: 600;
}

.candidate__stats {
  display: flex;
  gap: var(--space-5);
  padding: var(--space-3) 0;
  border-block: 1px solid var(--c-line);
  margin-bottom: var(--space-3);
}

dt {
  font-size: var(--text-xs);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--c-ink-muted);
}

dd {
  font-weight: 600;
}

.candidate__letter {
  font-size: var(--text-sm);
  color: var(--c-ink-soft);
  white-space: pre-line;
  margin-bottom: var(--space-4);
}

.hint {
  font-size: var(--text-sm);
}

.error {
  padding: var(--space-3);
  background: var(--c-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--c-danger);
  font-size: var(--text-sm);
}
</style>
