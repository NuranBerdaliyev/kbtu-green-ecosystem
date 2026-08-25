<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { tripsApi } from '@/api/trips'
import { useAuthStore } from '@/stores/auth'
import { useAsync } from '@/composables/useAsync'
import { parseWkt, haversineKm } from '@/utils/geo'
import { formatDate, formatDateTime, formatNumber } from '@/utils/format'
import { TRIP_STATUS, TRIP_PAYMENT_LABELS } from '@/utils/constants'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import LeafletMap from '@/components/map/LeafletMap.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const tripId = Number(route.params.id)

const trip = useAsync(() => tripsApi.findById(tripId))
const participants = useAsync(() => tripsApi.participants(tripId), [])

const acting = ref('')
const actionError = ref('')

const status = computed(() => trip.data.value?.tripStatus)
const isDriver = computed(() => trip.data.value?.driverId === auth.userId)
const price = computed(() => trip.data.value?.priceEcoCoins ?? 0)

/** Cancelled participations stay in the list, so filter them out. */
const activeParticipants = computed(() =>
  (participants.data.value ?? []).filter((p) => !p.isCancelled),
)
const myParticipation = computed(() =>
  activeParticipants.value.find((p) => p.passengerId === auth.userId),
)
const hasPassengers = computed(() => activeParticipants.value.length > 0)

const departed = computed(
  () => trip.data.value && new Date(trip.data.value.departureTime) <= new Date(),
)

const balance = computed(() => auth.stats?.ecoCoinsBalance ?? 0)
const canAfford = computed(() => balance.value >= price.value)

/*
 * TripParticipantService rules: only PUBLISHED trips can be joined, not after
 * the departure time, not by the driver, and the fare is reserved on join.
 */
const canJoin = computed(
  () =>
    status.value === TRIP_STATUS.PUBLISHED &&
    !isDriver.value &&
    !myParticipation.value &&
    !departed.value &&
    trip.data.value?.availableSeats > 0,
)

/** Leaving is only allowed while PUBLISHED; the fare is refunded. */
const canLeave = computed(
  () => status.value === TRIP_STATUS.PUBLISHED && !isDriver.value && Boolean(myParticipation.value),
)

/** Starting requires the departure time to have passed and at least one passenger. */
const canStart = computed(
  () =>
    isDriver.value &&
    status.value === TRIP_STATUS.PUBLISHED &&
    departed.value &&
    hasPassengers.value,
)

const points = computed(() => {
  const t = trip.data.value
  if (!t) return []
  const from = parseWkt(t.departureLocationWkt)
  const to = parseWkt(t.destinationLocationWkt)
  return [
    from && { id: 'from', ...from, color: '#B5791A', title: 'Отправление' },
    to && { id: 'to', ...to, color: '#2F6B4F', title: 'Назначение' },
  ].filter(Boolean)
})

const distanceKm = computed(() =>
  points.value.length === 2 ? haversineKm(points.value[0], points.value[1]) : null,
)

async function reload() {
  await Promise.all([trip.run(), participants.run()])
}

/**
 * join, leave, complete and cancel all move EcoCoins, so the header balance
 * is refreshed after them.
 */
const MONEY_ACTIONS = ['join', 'leave', 'complete', 'cancel']

async function act(name, fn) {
  acting.value = name
  actionError.value = ''
  try {
    await fn()
    await reload()
    if (MONEY_ACTIONS.includes(name)) await auth.loadStats()
  } catch (e) {
    actionError.value = e.message
  } finally {
    acting.value = ''
  }
}

async function removeTrip() {
  if (!window.confirm('Удалить поездку? Действие необратимо.')) return
  acting.value = 'delete'
  actionError.value = ''
  try {
    await tripsApi.remove(tripId)
    router.push({ name: 'trips' })
  } catch (e) {
    actionError.value = e.message
    acting.value = ''
  }
}

onMounted(reload)
</script>

<template>
  <div class="stack">
    <StateBlock
      :loading="trip.loading.value"
      :error="trip.error.value ?? ''"
      :skeletons="2"
      @retry="reload"
    >
      <template v-if="trip.data.value">
        <PageHeader
          eyebrow="Поездка"
          :title="formatDate(trip.data.value.departureTime)"
          :subtitle="`Выезд в ${formatDateTime(trip.data.value.departureTime).split(', ')[1]}`"
        >
          <template #actions>
            <StatusBadge :status="trip.data.value.tripStatus" />
          </template>
        </PageHeader>

        <div class="layout">
          <LeafletMap :markers="points" height="380px" />

          <aside class="stack">
            <div class="card stack">
              <dl class="facts">
                <div>
                  <dt>Цена с пассажира</dt>
                  <dd class="metric price">{{ formatNumber(price) }} EC</dd>
                </div>
                <div>
                  <dt>Свободные места</dt>
                  <dd class="metric">
                    {{ trip.data.value.availableSeats }} из {{ trip.data.value.totalSeats }}
                  </dd>
                </div>
                <div v-if="distanceKm">
                  <dt>Расстояние</dt>
                  <dd class="metric">≈ {{ distanceKm.toFixed(1) }} км</dd>
                </div>
                <div>
                  <dt>Водитель</dt>
                  <dd>{{ isDriver ? 'Вы' : `Пользователь #${trip.data.value.driverId}` }}</dd>
                </div>
              </dl>

              <p v-if="actionError" class="error">{{ actionError }}</p>

              <!-- Passenger actions -->
              <template v-if="!isDriver">
                <BaseButton
                  v-if="canJoin"
                  :disabled="!canAfford"
                  :loading="acting === 'join'"
                  @click="act('join', () => tripsApi.join(tripId))"
                >
                  Присоединиться за {{ formatNumber(price) }} EC
                </BaseButton>
                <p v-if="canJoin && !canAfford" class="warn">
                  Недостаточно EcoCoins: нужно {{ formatNumber(price) }}, у вас
                  {{ formatNumber(balance) }}.
                </p>

                <BaseButton
                  v-if="canLeave"
                  variant="ghost"
                  :loading="acting === 'leave'"
                  @click="act('leave', () => tripsApi.leave(tripId))"
                >
                  Отменить участие и вернуть {{ formatNumber(price) }} EC
                </BaseButton>

                <p v-if="myParticipation" class="text-muted note">
                  Оплата: {{ TRIP_PAYMENT_LABELS[myParticipation.paymentStatus] }} —
                  <span class="metric"
                    >{{ formatNumber(myParticipation.reservedEcoCoins) }} EC</span
                  >
                </p>
                <p v-else-if="status === TRIP_STATUS.PUBLISHED && departed" class="text-muted note">
                  Присоединиться нельзя: время выезда уже прошло.
                </p>
                <p
                  v-else-if="
                    status === TRIP_STATUS.PUBLISHED && trip.data.value.availableSeats === 0
                  "
                  class="text-muted note"
                >
                  Свободных мест не осталось.
                </p>
                <p v-else-if="status === TRIP_STATUS.CREATED" class="text-muted note">
                  Поездка ещё не опубликована водителем.
                </p>
              </template>

              <!-- Driver actions, one row per lifecycle stage -->
              <template v-else>
                <BaseButton
                  v-if="status === TRIP_STATUS.CREATED"
                  :loading="acting === 'publish'"
                  @click="act('publish', () => tripsApi.publish(tripId))"
                >
                  Опубликовать
                </BaseButton>

                <BaseButton
                  v-if="status === TRIP_STATUS.PUBLISHED"
                  :disabled="!canStart"
                  :loading="acting === 'start'"
                  @click="act('start', () => tripsApi.start(tripId))"
                >
                  Начать поездку
                </BaseButton>
                <p v-if="status === TRIP_STATUS.PUBLISHED && !departed" class="text-muted note">
                  Начать можно после времени выезда.
                </p>
                <p v-else-if="status === TRIP_STATUS.PUBLISHED && !hasPassengers" class="warn">
                  Нужен хотя бы один пассажир — без них поездку нельзя начать.
                </p>

                <BaseButton
                  v-if="status === TRIP_STATUS.IN_PROGRESS"
                  :loading="acting === 'complete'"
                  @click="act('complete', () => tripsApi.complete(tripId))"
                >
                  Завершить и получить {{ formatNumber(price * activeParticipants.length) }} EC
                </BaseButton>

                <BaseButton
                  v-if="
                    [TRIP_STATUS.CREATED, TRIP_STATUS.PUBLISHED, TRIP_STATUS.IN_PROGRESS].includes(
                      status,
                    )
                  "
                  variant="ghost"
                  :loading="acting === 'cancel'"
                  @click="act('cancel', () => tripsApi.cancel(tripId))"
                >
                  Отменить поездку
                </BaseButton>

                <BaseButton
                  v-if="status === TRIP_STATUS.CREATED"
                  variant="danger"
                  :loading="acting === 'delete'"
                  @click="removeTrip"
                >
                  Удалить
                </BaseButton>

                <p v-if="status === TRIP_STATUS.IN_PROGRESS" class="text-muted note">
                  После завершения вы получите оплату пассажиров, а ESG и CO₂ начислятся всем
                  участникам.
                </p>
                <p v-if="status === TRIP_STATUS.CANCELLED" class="text-muted note">
                  Поездка отменена, оплата пассажирам возвращена.
                </p>
              </template>
            </div>

            <div class="card stack">
              <h3>Пассажиры</h3>
              <p v-if="activeParticipants.length === 0" class="text-muted">
                Пока никто не присоединился.
              </p>
              <ul v-else class="passengers">
                <li v-for="p in activeParticipants" :key="p.id">
                  <span>
                    {{ p.passengerId === auth.userId ? 'Вы' : `Пользователь #${p.passengerId}` }}
                  </span>
                  <span class="text-muted">
                    {{ TRIP_PAYMENT_LABELS[p.paymentStatus] ?? formatDateTime(p.joinedAt) }}
                  </span>
                </li>
              </ul>
            </div>
          </aside>
        </div>

        <button class="back" type="button" @click="router.push({ name: 'trips' })">
          ← Ко всем поездкам
        </button>
      </template>
    </StateBlock>
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

.facts {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.facts > div {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: var(--space-3);
}

dt {
  font-size: var(--text-sm);
  color: var(--c-ink-muted);
}

dd {
  font-weight: 600;
}

.price {
  color: var(--c-coin);
}

.passengers {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.passengers li {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  font-size: var(--text-sm);
  padding-bottom: var(--space-2);
  border-bottom: 1px solid var(--c-line);
}

.note {
  font-size: var(--text-sm);
  line-height: 1.5;
}

.warn {
  padding: var(--space-3);
  background: var(--c-coin-soft);
  border-radius: var(--radius-sm);
  color: var(--c-coin);
  font-size: var(--text-sm);
}

.error {
  padding: var(--space-3);
  background: var(--c-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--c-danger);
  font-size: var(--text-sm);
}

.back {
  align-self: flex-start;
  border: none;
  background: none;
  padding: 0;
  color: var(--c-ink-muted);
  cursor: pointer;
}

.back:hover {
  color: var(--c-moss);
}
</style>
