<script setup>
import { computed, onMounted } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { formatNumber, formatCo2 } from '@/utils/format'
import { ESG_RATING_MAX, ROLES } from '@/utils/constants'

const auth = useAuthStore()
const route = useRoute()

/** Set by the router guard when a role-protected route was blocked. */
const forbidden = computed(() => Boolean(route.query.forbidden))

const modules = computed(() => {
  const base = [
    { to: 'trips', label: 'Совместные поездки', text: 'Найдите попутчиков до кампуса.' },
    { to: 'eco-map', label: 'Переработка отходов', text: 'Карта контейнеров и сдача отходов.' },
    { to: 'vacancies', label: 'Карьера', text: 'Вакансии партнёров университета.' },
  ]
  if (auth.hasRole(ROLES.HR)) {
    base.push({ to: 'my-vacancies', label: 'Мои вакансии', text: 'Публикация и отбор кандидатов.' })
  }
  if (auth.hasRole(ROLES.ADMIN)) {
    base.push({ to: 'admin', label: 'Администрирование', text: 'Метрики, контейнеры, роли.' })
  }
  return base
})

onMounted(auth.loadStats)
</script>

<template>
  <div class="stack">
    <section>
      <p class="eyebrow">Ваша активность</p>
      <h1>Здравствуйте, {{ auth.firstName || 'студент' }}</h1>
    </section>

    <p v-if="forbidden" class="forbidden">
      Этот раздел доступен другой роли. Если роль недавно изменили, войдите заново.
    </p>

    <section class="stats">
      <article class="card stat">
        <p class="text-muted">EcoCoins</p>
        <p class="metric stat__value">{{ formatNumber(auth.stats?.ecoCoinsBalance) }}</p>
      </article>
      <article class="card stat">
        <p class="text-muted">ESG-рейтинг</p>
        <p class="metric stat__value">
          {{ auth.stats?.esgRating ?? 0 }}<span class="stat__max">/{{ ESG_RATING_MAX }}</span>
        </p>
      </article>
      <article class="card stat">
        <p class="text-muted">Сокращено выбросов</p>
        <p class="metric stat__value">{{ formatCo2(auth.stats?.totalCo2Saved) }}</p>
      </article>
      <article class="card stat">
        <p class="text-muted">Место в рейтинге</p>
        <p class="metric stat__value">#{{ auth.stats?.leaderboardRank ?? '—' }}</p>
      </article>
    </section>

    <section class="modules">
      <RouterLink
        v-for="item in modules"
        :key="item.to"
        :to="{ name: item.to }"
        class="card module"
      >
        <h3>{{ item.label }}</h3>
        <p class="text-muted">{{ item.text }}</p>
      </RouterLink>
    </section>
  </div>
</template>

<style scoped>
.forbidden {
  padding: var(--space-3);
  background: var(--c-coin-soft);
  border-radius: var(--radius-sm);
  color: var(--c-coin);
  font-size: var(--text-sm);
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: var(--space-4);
}

.modules {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--space-4);
}

.stat__value {
  font-size: var(--text-2xl);
  font-weight: 600;
}

.stat__max {
  font-size: var(--text-lg);
  color: var(--c-ink-muted);
}

.module {
  transition:
    border-color 0.15s ease,
    transform 0.15s ease;
}

.module:hover {
  border-color: var(--c-moss);
  transform: translateY(-2px);
}
</style>
