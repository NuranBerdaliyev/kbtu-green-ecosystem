<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ecoPointsApi } from '@/api/ecoWaste'
import { useAuthStore } from '@/stores/auth'
import { useAsync } from '@/composables/useAsync'
import {
  WASTE_TYPE_LABELS,
  WASTE_TYPE_COLORS,
  GRAMS_PER_COIN,
  FULLNESS_MAX,
} from '@/utils/constants'
import { formatNumber } from '@/utils/format'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'
import FullnessBar from '@/components/common/FullnessBar.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const auth = useAuthStore()
const containers = useAsync(ecoPointsApi.activeContainers, [])

const selected = ref(null)
const submitting = ref(false)
const error = ref('')
const result = ref(null)

// Matches WasteDepositRequestDto.
const form = reactive({
  wasteWeightGrams: 500,
  addedFullnessPercentage: 5,
})

/** A container at 100% rejects deposits, so it cannot be chosen. */
const isFull = (container) => container.fullnessPercentage >= FULLNESS_MAX

const canSubmit = computed(
  () =>
    selected.value &&
    !isFull(selected.value) &&
    form.wasteWeightGrams >= 1 &&
    form.addedFullnessPercentage >= 1,
)

/**
 * The backend awards 1 coin per 100 g but never less than 1 per deposit,
 * so the preview floors at 1 rather than showing 0 for small amounts.
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
    result.value = await ecoPointsApi.deposit({
      qrCodeToken: selected.value.qrCodeToken,
      wasteWeightGrams: form.wasteWeightGrams,
      addedFullnessPercentage: form.addedFullnessPercentage,
    })
    await auth.loadStats()
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

    <section v-if="result" class="card success stack">
      <p class="eyebrow">Принято</p>
      <p class="metric success__coins">+{{ formatNumber(result.ecoCoinsEarned) }} EcoCoins</p>
      <p class="text-muted">Запись сохранена в истории активности.</p>
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
              <input v-model.number="form.wasteWeightGrams" type="number" min="1" />
            </label>

            <label class="field">
              Насколько заполнился контейнер, %
              <input
                v-model.number="form.addedFullnessPercentage"
                type="number"
                min="1"
                max="100"
              />
            </label>

            <p class="estimate">
              Примерно <span class="metric">+{{ estimatedCoins }}</span> EcoCoins
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

.success {
  border-color: var(--c-moss);
  background: var(--c-moss-soft);
}

.success__coins {
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--c-moss-dark);
}
</style>
