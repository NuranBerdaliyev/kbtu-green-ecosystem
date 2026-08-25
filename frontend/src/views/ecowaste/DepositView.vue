<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ecoPointsApi } from '@/api/ecoWaste'
import { useAsync } from '@/composables/useAsync'
import {
  WASTE_TYPE_LABELS,
  WASTE_TYPE_COLORS,
  GRAMS_PER_COIN,
  FULLNESS_MAX,
  MAX_DEPOSIT_GRAMS,
} from '@/utils/constants'
import { formatNumber } from '@/utils/format'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'
import FullnessBar from '@/components/common/FullnessBar.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'

const containers = useAsync(ecoPointsApi.activeContainers, [])

const selected = ref(null)
const submitting = ref(false)
const error = ref('')
const result = ref(null)

// Matches WasteDepositRequestDto — the backend no longer accepts a fullness
// delta; it derives it from the weight and the container capacity on approval.
const form = reactive({
  wasteWeightGrams: 500,
})

/** A container at 100% rejects deposits, so it cannot be chosen. */
const isFull = (container) => container.fullnessPercentage >= FULLNESS_MAX

/** Free capacity in grams — the backend rejects anything larger. */
const freeCapacity = computed(() => {
  const c = selected.value
  if (!c || c.capacityGrams == null) return null
  return Math.max(0, c.capacityGrams - (c.currentWeightGrams ?? 0))
})

const weightError = computed(() => {
  if (form.wasteWeightGrams < 1) return 'Укажите вес'
  if (form.wasteWeightGrams > MAX_DEPOSIT_GRAMS)
    return `Не более ${MAX_DEPOSIT_GRAMS} г за одну сдачу`
  if (freeCapacity.value != null && form.wasteWeightGrams > freeCapacity.value)
    return `В контейнере свободно ${freeCapacity.value} г`
  return ''
})

const canSubmit = computed(() => selected.value && !isFull(selected.value) && !weightError.value)

/**
 * Shown as a potential reward only. Nothing is granted until an admin
 * approves the deposit, so this must not read as a confirmed balance change.
 */
const estimatedCoins = computed(() =>
  Math.max(1, Math.floor(form.wasteWeightGrams / GRAMS_PER_COIN)),
)

function select(container) {
  if (isFull(container)) return
  selected.value = container
}

async function submit() {
  if (!canSubmit.value) return
  submitting.value = true
  error.value = ''
  try {
    // Returns a WasteLogResponseDto with status PENDING and no reward yet.
    result.value = await ecoPointsApi.deposit({
      qrCodeToken: selected.value.qrCodeToken,
      wasteWeightGrams: form.wasteWeightGrams,
    })
  } catch (e) {
    error.value = e.message
  } finally {
    submitting.value = false
  }
}

function reset() {
  result.value = null
  selected.value = null
  containers.run()
}

onMounted(containers.run)
</script>

<template>
  <div class="stack">
    <PageHeader eyebrow="Eco Waste" title="Сдать отходы" />

    <section v-if="result" class="card pending stack">
      <p class="eyebrow">Заявка отправлена</p>
      <p class="pending__lead">Ожидает подтверждения администратора</p>
      <p class="text-muted">
        Начисление появится после проверки. До этого контейнер и ваш баланс не меняются.
      </p>
      <dl class="pending__facts">
        <div>
          <dt>Вес</dt>
          <dd class="metric">{{ formatNumber(result.wasteWeightGrams) }} г</dd>
        </div>
        <div>
          <dt>Статус</dt>
          <dd><StatusBadge :status="result.status" kind="deposit" /></dd>
        </div>
      </dl>
      <div class="row">
        <BaseButton @click="reset">Сдать ещё</BaseButton>
        <RouterLink :to="{ name: 'profile' }">
          <BaseButton variant="ghost">В профиль</BaseButton>
        </RouterLink>
      </div>
    </section>

    <template v-else>
      <StateBlock
        :loading="containers.loading.value"
        :error="containers.error.value ?? ''"
        :empty="(containers.data.value ?? []).length === 0"
        empty-title="Нет активных контейнеров"
        @retry="containers.run"
      >
        <div class="layout">
          <div class="stack">
            <h2>1. Выберите контейнер</h2>
            <p class="text-muted hint">
              Выберите контейнер, в который сдаёте отходы. Вес указываете вы — автоматического
              подтверждения пока нет.
            </p>
            <ul class="list">
              <li
                v-for="container in containers.data.value"
                :key="container.id"
                :class="[
                  'card',
                  'bin',
                  {
                    'bin--selected': selected?.id === container.id,
                    'bin--full': isFull(container),
                  },
                ]"
                @click="select(container)"
              >
                <div class="bin__top">
                  <h3>{{ container.title }}</h3>
                  <span
                    class="swatch"
                    :style="{ background: WASTE_TYPE_COLORS[container.wasteType] }"
                  />
                </div>
                <p class="text-muted bin__type">
                  {{ WASTE_TYPE_LABELS[container.wasteType] }}
                  <span v-if="isFull(container)"> · заполнен, сдача недоступна</span>
                </p>
                <FullnessBar :value="container.fullnessPercentage" />
              </li>
            </ul>
          </div>

          <form class="card stack" @submit.prevent="submit">
            <h2>2. Укажите объём</h2>

            <label class="field">
              Вес отходов, граммы
              <input
                v-model.number="form.wasteWeightGrams"
                type="number"
                min="1"
                :max="MAX_DEPOSIT_GRAMS"
              />
              <span v-if="weightError" class="field__error">{{ weightError }}</span>
              <span v-else-if="freeCapacity != null" class="text-muted field__hint">
                Свободно {{ formatNumber(freeCapacity) }} г из
                {{ formatNumber(selected.capacityGrams) }} г
              </span>
            </label>

            <p class="estimate">
              После подтверждения — примерно
              <span class="metric">+{{ estimatedCoins }}</span> EcoCoins
              <span class="text-muted">— точную сумму рассчитает сервер</span>
            </p>

            <p class="disclaimer">
              Вес вводится вручную и не проверяется системой. Начисления за сдачу отходов носят
              демонстрационный характер.
            </p>

            <p v-if="error" class="error">{{ error }}</p>

            <BaseButton type="submit" :loading="submitting" :disabled="!canSubmit">
              {{ selected ? `Сдать в «${selected.title}»` : 'Выберите контейнер' }}
            </BaseButton>
          </form>
        </div>
      </StateBlock>
    </template>
  </div>
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 1fr 340px;
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
}

.hint {
  font-size: var(--text-sm);
}

.list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.bin {
  padding: var(--space-4);
  cursor: pointer;
}

.bin--selected {
  border-color: var(--c-moss);
  background: var(--c-moss-soft);
}

.bin--full {
  opacity: 0.55;
  cursor: not-allowed;
}

.bin__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bin__type {
  font-size: var(--text-sm);
  margin-bottom: var(--space-3);
}

.swatch {
  width: 10px;
  height: 10px;
  border-radius: 3px;
}

h3 {
  font-size: var(--text-base);
}

.field {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--c-ink-soft);
}

.field input {
  padding: var(--space-3);
  border: 1px solid var(--c-line-strong);
  border-radius: var(--radius-sm);
}

.estimate {
  padding: var(--space-3);
  background: var(--c-coin-soft);
  border-radius: var(--radius-sm);
  color: var(--c-coin);
  font-size: var(--text-sm);
  font-weight: 600;
}

.disclaimer {
  font-size: var(--text-xs);
  color: var(--c-ink-muted);
  line-height: 1.5;
}

.error {
  padding: var(--space-3);
  background: var(--c-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--c-danger);
  font-size: var(--text-sm);
}

.pending {
  border-color: var(--c-coin);
  background: var(--c-coin-soft);
}

.pending .eyebrow {
  color: var(--c-coin);
}

.pending__lead {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 600;
  color: var(--c-coin);
}

.pending__facts {
  display: flex;
  gap: var(--space-5);
  padding: var(--space-3) 0;
}

.pending__facts dt {
  font-size: var(--text-xs);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--c-ink-muted);
}

.field__error {
  color: var(--c-danger);
  font-size: var(--text-sm);
}

.field__hint {
  font-size: var(--text-xs);
}
</style>
