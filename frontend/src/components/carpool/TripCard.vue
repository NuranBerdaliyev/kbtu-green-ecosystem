<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { parseWkt, haversineKm } from '@/utils/geo'
import { formatDate, formatDateTime } from '@/utils/format'
import StatusBadge from '@/components/common/StatusBadge.vue'

const props = defineProps({
  trip: { type: Object, required: true },
})

const distanceKm = computed(() => {
  const from = parseWkt(props.trip.departureLocationWkt)
  const to = parseWkt(props.trip.destinationLocationWkt)
  return from && to ? haversineKm(from, to) : null
})
</script>

<template>
  <RouterLink :to="{ name: 'trip-details', params: { id: trip.id } }" class="card trip">
    <div class="trip__top">
      <div>
        <p class="trip__date">{{ formatDate(trip.departureTime) }}</p>
        <p class="metric trip__time">{{ formatDateTime(trip.departureTime).split(', ')[1] }}</p>
      </div>
      <StatusBadge :status="trip.tripStatus" />
    </div>

    <dl class="trip__meta">
      <div>
        <dt>Мест</dt>
        <dd class="metric">{{ trip.availableSeats }} из {{ trip.totalSeats }}</dd>
      </div>
      <div v-if="distanceKm">
        <dt>Расстояние</dt>
        <dd class="metric">≈ {{ distanceKm.toFixed(1) }} км</dd>
      </div>
      <div>
        <dt>Цена</dt>
        <dd class="metric trip__price">{{ trip.priceEcoCoins }} EC</dd>
      </div>
    </dl>
  </RouterLink>
</template>

<style scoped>
.trip {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  transition:
    border-color 0.15s ease,
    transform 0.15s ease;
}

.trip:hover {
  border-color: var(--c-moss);
  transform: translateY(-2px);
}

.trip__top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
}

.trip__date {
  font-size: var(--text-sm);
  color: var(--c-ink-muted);
}

.trip__time {
  font-size: var(--text-xl);
  font-weight: 600;
}

.trip__meta {
  display: flex;
  gap: var(--space-5);
}

dt {
  font-size: var(--text-xs);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--c-ink-muted);
}

dd {
  font-size: var(--text-sm);
}

.trip__price {
  color: var(--c-coin);
  font-weight: 600;
}
</style>
