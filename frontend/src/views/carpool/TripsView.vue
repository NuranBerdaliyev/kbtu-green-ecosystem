<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { tripsApi } from '@/api/trips'
import { useAsync } from '@/composables/useAsync'
import { CAMPUS_CENTER } from '@/utils/constants'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import TripCard from '@/components/carpool/TripCard.vue'

// Matches TripSearchRequestDto. Empty values are stripped before sending.
const filters = reactive({
  minSeats: 1,
  radiusKm: 10,
  useLocation: true,
})

const tab = ref('search') // search | my | joined

const search = useAsync(() => {
  const params = { page: 0, size: 20, sort: 'departureTime,asc' }
  if (filters.minSeats) params.minSeats = filters.minSeats
  if (filters.useLocation) {
    params.originLat = CAMPUS_CENTER.lat
    params.originLng = CAMPUS_CENTER.lng
    params.radiusKm = filters.radiusKm
  }
  return tripsApi.search(params)
})

const mine = useAsync(tripsApi.myTrips, [])
const joined = useAsync(tripsApi.joinedTrips, [])

function load() {
  if (tab.value === 'search') search.run()
  else if (tab.value === 'my') mine.run()
  else joined.run()
}

function switchTab(next) {
  tab.value = next
  load()
}

onMounted(load)

// The search endpoint returns a Spring Page; my/joined return plain arrays.
const listFor = (name) =>
  ({
    search: search.data.value?.content ?? [],
    my: mine.data.value ?? [],
    joined: joined.data.value ?? [],
  })[name]

// Each tab has its own request, so it must show its own loading and error.
const active = computed(() => ({ search, my: mine, joined })[tab.value])
</script>

<template>
  <div class="stack">
    <PageHeader
      eyebrow="Carpool"
      title="Поездки"
      subtitle="Найдите попутчиков или предложите свободные места."
    >
      <template #actions>
        <RouterLink :to="{ name: 'trip-create' }">
          <BaseButton>Создать поездку</BaseButton>
        </RouterLink>
      </template>
    </PageHeader>

    <nav class="tabs">
      <button :class="{ active: tab === 'search' }" type="button" @click="switchTab('search')">
        Все поездки
      </button>
      <button :class="{ active: tab === 'my' }" type="button" @click="switchTab('my')">
        Мои поездки
      </button>
      <button :class="{ active: tab === 'joined' }" type="button" @click="switchTab('joined')">
        Я присоединился
      </button>
    </nav>

    <div v-if="tab === 'search'" class="card filters">
      <label class="filters__field">
        Минимум мест
        <input v-model.number="filters.minSeats" type="number" min="1" max="8" @change="load" />
      </label>
      <label class="filters__field">
        Радиус, км
        <input
          v-model.number="filters.radiusKm"
          type="number"
          min="1"
          max="50"
          :disabled="!filters.useLocation"
          @change="load"
        />
      </label>
      <label class="filters__check">
        <input v-model="filters.useLocation" type="checkbox" @change="load" />
        Рядом с кампусом
      </label>
    </div>

    <StateBlock
      :loading="active.loading.value"
      :error="active.error.value ?? ''"
      :empty="listFor(tab).length === 0"
      empty-title="Поездок не найдено"
      empty-text="Попробуйте увеличить радиус или создайте свою поездку."
      @retry="load"
    >
      <div class="grid">
        <TripCard v-for="trip in listFor(tab)" :key="trip.id" :trip="trip" />
      </div>
    </StateBlock>
  </div>
</template>

<style scoped>
.tabs {
  display: flex;
  gap: var(--space-2);
  border-bottom: 1px solid var(--c-line);
}

.tabs button {
  padding: var(--space-3) var(--space-4);
  border: none;
  background: none;
  border-bottom: 2px solid transparent;
  color: var(--c-ink-muted);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
}

.tabs button.active {
  color: var(--c-moss);
  border-bottom-color: var(--c-moss);
}

.filters {
  display: flex;
  align-items: end;
  gap: var(--space-5);
  flex-wrap: wrap;
  padding: var(--space-4);
}

.filters__field {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--c-ink-soft);
}

.filters__field input {
  width: 120px;
  padding: var(--space-2);
  border: 1px solid var(--c-line-strong);
  border-radius: var(--radius-sm);
}

.filters__check {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--c-ink-soft);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--space-4);
}
</style>
