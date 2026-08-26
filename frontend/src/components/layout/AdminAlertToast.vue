<script setup>
import { onUnmounted, ref } from 'vue'
import { useEcoContainerSocket } from '@/composables/useEcoContainerSocket'

const notice = ref(null)
let hideTimer = null

function hide() {
  notice.value = null
  if (hideTimer) {
    window.clearTimeout(hideTimer)
    hideTimer = null
  }
}

useEcoContainerSocket({
  onAlert(payload) {
    notice.value =
      typeof payload === 'string'
        ? { text: payload, detail: '' }
        : {
            text: `Контейнер «${payload.title}» требует вывоза`,
            detail: `${payload.previousFullnessPercentage}% → ${payload.currentFullnessPercentage}% · ${payload.currentWeightGrams} / ${payload.capacityGrams} г`,
          }

    if (hideTimer) window.clearTimeout(hideTimer)
    hideTimer = window.setTimeout(hide, 12_000)
  },
})

onUnmounted(() => {
  if (hideTimer) window.clearTimeout(hideTimer)
})
</script>

<template>
  <aside v-if="notice" class="admin-alert" role="alert" aria-live="assertive">
    <div>
      <p class="admin-alert__title">{{ notice.text }}</p>
      <p v-if="notice.detail" class="admin-alert__detail">{{ notice.detail }}</p>
    </div>
    <button type="button" class="admin-alert__close" aria-label="Закрыть" @click="hide">×</button>
  </aside>
</template>

<style scoped>
.admin-alert {
  position: fixed;
  top: calc(var(--header-height) + var(--space-4));
  right: var(--space-5);
  z-index: 100;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  width: min(420px, calc(100vw - 32px));
  padding: var(--space-4);
  border: 1px solid var(--c-danger);
  border-radius: var(--radius);
  background: var(--c-danger-soft);
  color: var(--c-danger);
  box-shadow: 0 10px 30px rgb(0 0 0 / 14%);
}

.admin-alert__title {
  font-weight: 700;
}

.admin-alert__detail {
  margin-top: var(--space-1);
  font-size: var(--text-sm);
}

.admin-alert__close {
  border: 0;
  background: transparent;
  color: inherit;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
}
</style>
