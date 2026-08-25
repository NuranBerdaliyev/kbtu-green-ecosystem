<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { tripsApi } from '@/api/trips'
import { toWkt, haversineKm } from '@/utils/geo'
import { toDateTimeLocal, toLocalDateTime } from '@/utils/format'
import { CAMPUS_CENTER, TRIP_PRICE_MIN, TRIP_PRICE_MAX, TRIP_SEATS_MAX } from '@/utils/constants'
import { useAuthStore } from '@/stores/auth'
import PageHeader from '@/components/common/PageHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import LeafletMap from '@/components/map/LeafletMap.vue'

const router = useRouter()
const auth = useAuthStore()

const picking = ref('departure') // departure | destination
const departure = ref(null)
const destination = ref({ ...CAMPUS_CENTER }) // campus is the usual destination

// Default to tomorrow morning; the backend requires a future time.
const tomorrow = new Date(Date.now() + 24 * 60 * 60 * 1000)
tomorrow.setHours(8, 30, 0, 0)

const form = reactive({
  departureTime: toDateTimeLocal(tomorrow),
  totalSeats: 3,
  // Passengers pay this from their balance when they join; the driver
  // receives the total once the trip is completed.
  priceEcoCoins: 5,
})

const submitting = ref(false)
const error = ref('')
const fieldErrors = ref({})

const markers = computed(() =>
  [
    departure.value && {
      id: 'departure',
      ...departure.value,
      color: '#B5791A',
      title: 'Точка отправления',
    },
    destination.value && {
      id: 'destination',
      ...destination.value,
      color: '#2F6B4F',
      title: 'Точка назначения',
    },
  ].filter(Boolean),
)

const distanceKm = computed(() =>
  departure.value && destination.value ? haversineKm(departure.value, destination.value) : null,
)

const isFuture = computed(() => new Date(form.departureTime) > new Date())
const priceValid = computed(
  () => form.priceEcoCoins >= TRIP_PRICE_MIN && form.priceEcoCoins <= TRIP_PRICE_MAX,
)
const canSubmit = computed(
  () => departure.value && destination.value && isFuture.value && priceValid.value,
)

function onPick(point) {
  if (picking.value === 'departure') {
    departure.value = point
    picking.value = 'destination'
  } else {
    destination.value = point
    picking.value = 'departure'
  }
}

async function submit() {
  if (!canSubmit.value) return
  submitting.value = true
  error.value = ''
  fieldErrors.value = {}
  try {
    // driverId is taken from the JWT, so it is not part of the body.
    const trip = await tripsApi.create({
      departureLocationWkt: toWkt(departure.value),
      destinationLocationWkt: toWkt(destination.value),
      departureTime: toLocalDateTime(form.departureTime),
      totalSeats: form.totalSeats,
      priceEcoCoins: form.priceEcoCoins,
    })
    router.push({ name: 'trip-details', params: { id: trip.id } })
  } catch (e) {
    error.value = e.message
    fieldErrors.value = e.fieldErrors ?? {}
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="stack">
    <PageHeader
      eyebrow="Carpool"
      title="Новая поездка"
      subtitle="Отметьте на карте, откуда выезжаете и куда едете."
    />

    <div class="layout">
      <div class="stack">
        <p class="hint">
          {{
            picking === 'departure'
              ? 'Кликните по карте — отметим точку отправления'
              : 'Теперь кликните точку назначения'
          }}
        </p>
        <LeafletMap :markers="markers" pickable height="440px" @pick="onPick" />
      </div>

      <form class="card stack form" @submit.prevent="submit">
        <div class="points">
          <button
            type="button"
            :class="{ active: picking === 'departure' }"
            @click="picking = 'departure'"
          >
            <span class="dot dot--from" />
            Отправление
            <em>{{ departure ? 'отмечено' : 'не выбрано' }}</em>
          </button>
          <button
            type="button"
            :class="{ active: picking === 'destination' }"
            @click="picking = 'destination'"
          >
            <span class="dot dot--to" />
            Назначение
            <em>{{ destination ? 'отмечено' : 'не выбрано' }}</em>
          </button>
        </div>

        <p v-if="distanceKm" class="text-muted">
          Расстояние ≈ <span class="metric">{{ distanceKm.toFixed(1) }} км</span>
        </p>

        <label class="field">
          Дата и время выезда
          <input v-model="form.departureTime" type="datetime-local" />
          <span v-if="!isFuture" class="field__error">Время должно быть в будущем</span>
          <span v-else-if="fieldErrors.departureTime" class="field__error">
            {{ fieldErrors.departureTime }}
          </span>
        </label>

        <label class="field">
          Всего мест
          <input v-model.number="form.totalSeats" type="number" min="1" :max="TRIP_SEATS_MAX" />
          <span v-if="fieldErrors.totalSeats" class="field__error">{{
            fieldErrors.totalSeats
          }}</span>
        </label>

        <label class="field">
          Цена с пассажира, EcoCoins
          <input
            v-model.number="form.priceEcoCoins"
            type="number"
            :min="TRIP_PRICE_MIN"
            :max="TRIP_PRICE_MAX"
          />
          <span v-if="fieldErrors.priceEcoCoins" class="field__error">
            {{ fieldErrors.priceEcoCoins }}
          </span>
          <span v-else-if="!priceValid" class="field__error">
            От {{ TRIP_PRICE_MIN }} до {{ TRIP_PRICE_MAX }} EcoCoins
          </span>
        </label>

        <p class="text-muted note">
          Пассажир платит {{ form.priceEcoCoins }} EcoCoins при присоединении. Вы получите
          <span class="metric">{{ form.priceEcoCoins * (form.totalSeats || 0) }}</span> EcoCoins,
          если займут все места. Ваш баланс:
          <span class="metric">{{ auth.stats?.ecoCoinsBalance ?? 0 }}</span>
        </p>

        <p v-if="error" class="error">{{ error }}</p>

        <BaseButton type="submit" :loading="submitting" :disabled="!canSubmit">
          Создать поездку
        </BaseButton>
      </form>
    </div>
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

.hint {
  font-size: var(--text-sm);
  color: var(--c-moss-dark);
  background: var(--c-moss-soft);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-sm);
}

.points {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.points button {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3);
  border: 1px solid var(--c-line-strong);
  border-radius: var(--radius-sm);
  background: var(--c-surface);
  font-size: var(--text-sm);
  cursor: pointer;
  text-align: left;
}

.points button.active {
  border-color: var(--c-moss);
  background: var(--c-moss-soft);
}

.points em {
  margin-left: auto;
  font-style: normal;
  color: var(--c-ink-muted);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot--from {
  background: var(--c-coin);
}
.dot--to {
  background: var(--c-moss);
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

.field__error {
  color: var(--c-danger);
}

.note {
  font-size: var(--text-sm);
  line-height: 1.5;
}

.error {
  padding: var(--space-3);
  background: var(--c-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--c-danger);
  font-size: var(--text-sm);
}
</style>
