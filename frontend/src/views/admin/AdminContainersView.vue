<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { containersAdminApi } from '@/api/ecoWaste'
import { useAsync } from '@/composables/useAsync'
import { useEcoContainerSocket } from '@/composables/useEcoContainerSocket'
import { parseWkt, toWkt } from '@/utils/geo'
import { WASTE_TYPES, WASTE_TYPE_LABELS, WASTE_TYPE_COLORS, CAMPUS_CENTER } from '@/utils/constants'
import { formatNumber } from '@/utils/format'
import PageHeader from '@/components/common/PageHeader.vue'
import StateBlock from '@/components/common/StateBlock.vue'
import FullnessBar from '@/components/common/FullnessBar.vue'
import BaseInput from '@/components/common/BaseInput.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import LeafletMap from '@/components/map/LeafletMap.vue'

const containers = useAsync(containersAdminApi.findAll, [])

const editingId = ref(null)
const creating = ref(false)
const saving = ref(false)
const error = ref('')
const fieldErrors = ref({})

/*
 * capacityGrams replaced fullnessPercentage in the request DTO: fullness is
 * now derived by the backend from currentWeightGrams / capacityGrams and must
 * never be set by the client.
 */
const form = reactive({
  title: '',
  wasteType: WASTE_TYPES.PLASTIC,
  capacityGrams: 1000,
  isActive: true,
  qrCodeToken: '',
  point: { ...CAMPUS_CENTER },
})

const isFormOpen = computed(() => creating.value || editingId.value !== null)

useEcoContainerSocket({
  onContainer(updated) {
    const list = containers.data.value ?? []
    containers.data.value = list.map((c) => (c.id === updated.id ? updated : c))
  },
})

const markers = computed(() => {
  if (isFormOpen.value) {
    return [{ id: 'new', ...form.point, color: '#B5791A', title: form.title || 'Новый контейнер' }]
  }
  return (containers.data.value ?? [])
    .map((c) => {
      const point = parseWkt(c.locationWkt)
      return point && { id: c.id, ...point, color: WASTE_TYPE_COLORS[c.wasteType], title: c.title }
    })
    .filter(Boolean)
})

function openCreate() {
  Object.assign(form, {
    title: '',
    wasteType: WASTE_TYPES.PLASTIC,
    capacityGrams: 1000,
    isActive: true,
    qrCodeToken: '',
    point: { ...CAMPUS_CENTER },
  })
  editingId.value = null
  creating.value = true
  error.value = ''
  fieldErrors.value = {}
}

function openEdit(container) {
  Object.assign(form, {
    title: container.title,
    wasteType: container.wasteType,
    capacityGrams: container.capacityGrams ?? 1000,
    isActive: container.isActive,
    qrCodeToken: container.qrCodeToken,
    point: parseWkt(container.locationWkt) ?? { ...CAMPUS_CENTER },
  })
  creating.value = false
  editingId.value = container.id
  error.value = ''
  fieldErrors.value = {}
  editingContainer.value = container
}

/** Kept so the form can enforce the backend's edit restrictions. */
const editingContainer = ref(null)

/** Capacity cannot drop below the weight already inside. */
const minCapacity = computed(() => editingContainer.value?.currentWeightGrams ?? 1)

/** Waste type is locked until the container is emptied. */
const wasteTypeLocked = computed(() => (editingContainer.value?.currentWeightGrams ?? 0) > 0)

function close() {
  creating.value = false
  editingId.value = null
}

async function save() {
  saving.value = true
  error.value = ''
  fieldErrors.value = {}
  try {
    const payload = {
      title: form.title,
      locationWkt: toWkt(form.point),
      wasteType: form.wasteType,
      capacityGrams: form.capacityGrams,
      isActive: form.isActive,
      qrCodeToken: form.qrCodeToken,
    }
    if (editingId.value) await containersAdminApi.update(editingId.value, payload)
    else await containersAdminApi.create(payload)
    await containers.run()
    close()
  } catch (e) {
    error.value = e.message
    fieldErrors.value = e.fieldErrors ?? {}
  } finally {
    saving.value = false
  }
}

/** Dedicated endpoint: resets weight and fullness and broadcasts the change. */
async function empty(container) {
  if (!window.confirm(`Отметить «${container.title}» как вывезенный?`)) return
  error.value = ''
  try {
    await containersAdminApi.empty(container.id)
    await containers.run()
  } catch (e) {
    error.value = e.message
  }
}

/** A container with deposit history cannot be deleted — deactivate instead. */
async function remove(container) {
  if (!window.confirm(`Удалить «${container.title}»?`)) return
  try {
    await containersAdminApi.remove(container.id)
    await containers.run()
  } catch (e) {
    error.value = e.message
  }
}

const canSave = computed(
  () => form.title.trim() && form.qrCodeToken.trim() && form.capacityGrams >= minCapacity.value,
)

onMounted(containers.run)
</script>

<template>
  <div class="stack">
    <PageHeader eyebrow="Администрирование" title="Контейнеры">
      <template #actions>
        <BaseButton v-if="!isFormOpen" @click="openCreate">Добавить контейнер</BaseButton>
      </template>
    </PageHeader>

    <div class="layout">
      <div class="stack">
        <p v-if="isFormOpen" class="hint">Кликните по карте, чтобы поставить контейнер.</p>
        <LeafletMap
          :markers="markers"
          :pickable="isFormOpen"
          height="440px"
          @pick="form.point = $event"
        />
      </div>

      <section v-if="isFormOpen" class="card stack">
        <h2>{{ editingId ? 'Редактирование' : 'Новый контейнер' }}</h2>

        <BaseInput v-model="form.title" label="Название" :error="fieldErrors.title" />
        <BaseInput
          v-model="form.qrCodeToken"
          label="QR-токен"
          placeholder="KGE-PLA-006"
          :error="fieldErrors.qrCodeToken"
        />

        <label class="field">
          Тип отходов
          <select v-model="form.wasteType" :disabled="wasteTypeLocked">
            <option v-for="type in Object.values(WASTE_TYPES)" :key="type" :value="type">
              {{ WASTE_TYPE_LABELS[type] }}
            </option>
          </select>
          <span v-if="wasteTypeLocked" class="text-muted field__hint">
            Тип нельзя изменить, пока контейнер не вывезен.
          </span>
        </label>

        <label class="field">
          Вместимость, граммы
          <input v-model.number="form.capacityGrams" type="number" :min="minCapacity" />
          <span v-if="fieldErrors.capacityGrams" class="field__error">
            {{ fieldErrors.capacityGrams }}
          </span>
          <span v-else-if="form.capacityGrams < minCapacity" class="field__error">
            Не меньше текущего веса — {{ minCapacity }} г
          </span>
        </label>

        <label class="check">
          <input v-model="form.isActive" type="checkbox" />
          Активен и виден студентам
        </label>

        <p v-if="error" class="error">{{ error }}</p>
        <div class="row">
          <BaseButton :loading="saving" :disabled="!canSave" @click="save">Сохранить</BaseButton>
          <BaseButton variant="ghost" @click="close">Отмена</BaseButton>
        </div>
      </section>

      <StateBlock
        v-else
        :loading="containers.loading.value"
        :error="containers.error.value ?? ''"
        :empty="(containers.data.value ?? []).length === 0"
        empty-title="Контейнеров нет"
        @retry="containers.run"
      >
        <ul class="list">
          <li v-for="c in containers.data.value" :key="c.id" class="card bin">
            <div class="bin__top">
              <h3>{{ c.title }}</h3>
              <span class="swatch" :style="{ background: WASTE_TYPE_COLORS[c.wasteType] }" />
            </div>
            <p class="text-muted bin__meta">
              {{ WASTE_TYPE_LABELS[c.wasteType] }} · {{ c.isActive ? 'активен' : 'отключён' }}
              <span v-if="c.capacityGrams" class="metric">
                · {{ formatNumber(c.currentWeightGrams) }} / {{ formatNumber(c.capacityGrams) }} г
              </span>
            </p>
            <FullnessBar :value="c.fullnessPercentage" />
            <div class="row bin__actions">
              <BaseButton variant="ghost" @click="openEdit(c)">Изменить</BaseButton>
              <BaseButton v-if="c.currentWeightGrams > 0" variant="ghost" @click="empty(c)">
                Вывезен
              </BaseButton>
              <BaseButton variant="danger" @click="remove(c)">Удалить</BaseButton>
            </div>
          </li>
        </ul>
      </StateBlock>
    </div>
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
}

h3 {
  font-size: var(--text-base);
}

.hint {
  font-size: var(--text-sm);
  color: var(--c-moss-dark);
  background: var(--c-moss-soft);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-sm);
}

.list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  max-height: 440px;
  overflow-y: auto;
}

.bin {
  padding: var(--space-4);
}

.bin__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bin__meta {
  font-size: var(--text-sm);
  margin-bottom: var(--space-3);
}

.bin__actions {
  margin-top: var(--space-3);
  flex-wrap: wrap;
}

.swatch {
  width: 10px;
  height: 10px;
  border-radius: 3px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--c-ink-soft);
}

.field select,
.field input {
  padding: var(--space-3);
  border: 1px solid var(--c-line-strong);
  border-radius: var(--radius-sm);
  background: var(--c-surface);
}

.check {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--c-ink-soft);
}

.field__error {
  color: var(--c-danger);
  font-size: var(--text-sm);
}

.field__hint {
  font-size: var(--text-xs);
}

.error {
  padding: var(--space-3);
  background: var(--c-danger-soft);
  border-radius: var(--radius-sm);
  color: var(--c-danger);
  font-size: var(--text-sm);
}
</style>
