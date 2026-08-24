<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ecoPointsApi } from '@/api/ecoWaste'
import { useAsync } from '@/composables/useAsync'
import { useEcoContainerSocket } from '@/composables/useEcoContainerSocket'
import { parseWkt } from '@/utils/geo'
import { WASTE_TYPE_LABELS, WASTE_TYPE_COLORS, WASTE_TYPES } from '@/utils/constants'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'
import FullnessBar from '@/components/common/FullnessBar.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import LeafletMap from '@/components/map/LeafletMap.vue'

const containers = useAsync(ecoPointsApi.activeContainers, [])
const selectedId = ref(null)
const typeFilter = ref('ALL')
const liveUpdatedId = ref(null)

/**
 * Every deposit broadcasts the updated container on /topic/eco-containers,
 * so the list reflects new fullness without a refresh.
 */
useEcoContainerSocket({
  onContainer(updated) {
    const list = containers.data.value ?? []
    const index = list.findIndex((c) => c.id === updated.id)
    containers.data.value =
      index === -1 ? [...list, updated] : list.map((c) => (c.id === updated.id ? updated : c))
    liveUpdatedId.value = updated.id
    setTimeout(() => {
      if (liveUpdatedId.value === updated.id) liveUpdatedId.value = null
    }, 2000)
  },
})

const visible = computed(() =>
  (containers.data.value ?? []).filter(
    (c) => typeFilter.value === 'ALL' || c.wasteType === typeFilter.value,
  ),
)

const markers = computed(() =>
  visible.value
    .map((c) => {
      const point = parseWkt(c.locationWkt)
      return (
        point && {
          id: c.id,
          ...point,
          color: WASTE_TYPE_COLORS[c.wasteType],
          title: `${c.title} — ${c.fullnessPercentage}%`,
          selected: c.id === selectedId.value,
        }
      )
    })
    .filter(Boolean),
)

onMounted(containers.run)
</script>

<template>
  <div class="stack">
    <PageHeader
      eyebrow="Eco Waste"
      title="Карта контейнеров"
      subtitle="Заполненность обновляется автоматически после каждой сдачи."
    >
      <template #actions>
        <RouterLink :to="{ name: 'deposit' }">
          <BaseButton>Сдать отходы</BaseButton>
        </RouterLink>
      </template>
    </PageHeader>

    <div class="filters">
      <button :class="{ active: typeFilter === 'ALL' }" type="button" @click="typeFilter = 'ALL'">
        Все
      </button>
      <button
        v-for="type in Object.values(WASTE_TYPES)"
        :key="type"
        :class="{ active: typeFilter === type }"
        type="button"
        @click="typeFilter = type"
      >
        <span class="swatch" :style="{ background: WASTE_TYPE_COLORS[type] }" />
        {{ WASTE_TYPE_LABELS[type] }}
      </button>
    </div>

    <StateBlock
      :loading="containers.loading.value"
      :error="containers.error.value ?? ''"
      :empty="visible.length === 0"
      empty-title="Контейнеров нет"
      empty-text="Для этого типа отходов пока не добавлены контейнеры."
      @retry="containers.run"
    >
      <div class="layout">
        <LeafletMap :markers="markers" height="520px" @select="selectedId = $event" />

        <ul class="list">
          <li
            v-for="container in visible"
            :key="container.id"
            :class="[
              'card',
              'bin',
              {
                'bin--selected': container.id === selectedId,
                'bin--live': container.id === liveUpdatedId,
              },
            ]"
            @click="selectedId = container.id"
          >
            <div class="bin__top">
              <h3>{{ container.title }}</h3>
              <span
                class="swatch"
                :style="{ background: WASTE_TYPE_COLORS[container.wasteType] }"
              />
            </div>
            <p class="text-muted bin__type">{{ WASTE_TYPE_LABELS[container.wasteType] }}</p>
            <FullnessBar :value="container.fullnessPercentage" />
          </li>
        </ul>
      </div>
    </StateBlock>
  </div>
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: var(--space-5);
  align-items: start;
}

@media (max-width: 900px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

.filters {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.filters button {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--c-line-strong);
  border-radius: 999px;
  background: var(--c-surface);
  font-size: var(--text-sm);
  cursor: pointer;
}

.filters button.active {
  border-color: var(--c-moss);
  background: var(--c-moss-soft);
  color: var(--c-moss-dark);
  font-weight: 600;
}

.swatch {
  width: 10px;
  height: 10px;
  border-radius: 3px;
}

.list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  max-height: 520px;
  overflow-y: auto;
}

.bin {
  padding: var(--space-4);
  cursor: pointer;
  transition:
    border-color 0.15s ease,
    background-color 0.6s ease;
}

.bin__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-2);
}

.bin__type {
  font-size: var(--text-sm);
  margin-bottom: var(--space-3);
}

.bin--selected {
  border-color: var(--c-moss);
}

.bin--live {
  background: var(--c-moss-soft);
}

h3 {
  font-size: var(--text-base);
}
</style>
