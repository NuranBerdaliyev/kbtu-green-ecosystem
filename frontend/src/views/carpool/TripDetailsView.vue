<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { tripsApi } from '@/api/trips'
import { useAuthStore } from '@/stores/auth'
import { useAsync } from '@/composables/useAsync'
import { parseWkt, haversineKm } from '@/utils/geo'
import { formatDate, formatDateTime } from '@/utils/format'
import { TRIP_STATUS } from '@/utils/constants'
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

const isDriver = computed(() => trip.data.value?.driverId === auth.userId)
const hasJoined = computed(() =>
  (participants.data.value ?? []).some((p) => p.passengerId === auth.userId),
)
const canJoin = computed(
  () =>
    trip.data.value &&
    !isDriver.value &&
    !hasJoined.value &&
    trip.data.value.availableSeats > 0 &&
    [TRIP_STATUS.CREATED, TRIP_STATUS.ACTIVE].includes(trip.data.value.tripStatus),
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

/** Every action returns the updated trip, so we refresh both lists after. */
async function act(name, fn) {
  acting.value = name
  actionError.value = ''
  try {
    await fn()
    await reload()
  } catch (e) {
    actionError.value = e.message
  } finally {
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

              <BaseButton
                v-if="canJoin"
                :loading="acting === 'join'"
                @click="act('join', () => tripsApi.join(tripId))"
              >
                Присоединиться
              </BaseButton>

              <BaseButton
                v-else-if="hasJoined && !isDriver"
                variant="ghost"
                :loading="acting === 'leave'"
                @click="act('leave', () => tripsApi.leave(tripId))"
              >
                Отменить участие
              </BaseButton>

              <template v-if="isDriver">
                <BaseButton
                  v-if="trip.data.value.tripStatus === 'CREATED'"
                  :loading="acting === 'activate'"
                  @click="act('activate', () => tripsApi.activate(tripId))"
                >
                  Начать поездку
                </BaseButton>
                <BaseButton
                  v-if="trip.data.value.tripStatus === 'ACTIVE'"
                  :loading="acting === 'complete'"
                  @click="act('complete', () => tripsApi.complete(tripId))"
                >
                  Завершить поездку
                </BaseButton>
                <BaseButton
                  v-if="['CREATED', 'ACTIVE'].includes(trip.data.value.tripStatus)"
                  variant="danger"
                  :loading="acting === 'cancel'"
                  @click="act('cancel', () => tripsApi.cancel(tripId))"
                >
                  Отменить поездку
                </BaseButton>
              </template>

              <p v-if="isDriver && trip.data.value.tripStatus === 'ACTIVE'" class="text-muted note">
                EcoCoins начисляются всем участникам после завершения поездки.
              </p>
            </div>

            <div class="card stack">
              <h3>Пассажиры</h3>
              <p v-if="(participants.data.value ?? []).length === 0" class="text-muted">
                Пока никто не присоединился.
              </p>
              <ul v-else class="passengers">
                <li v-for="p in participants.data.value" :key="p.id">
                  <span>{{ p.passengerId === auth.userId ? 'Вы' : `Пользователь #${p.passengerId}` }}</span>
                  <span class="text-muted">{{ formatDateTime(p.joinedAt) }}</span>
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
