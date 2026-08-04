<script setup>
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useGamificationStore } from '@/stores/gamification'
import { formatNumber, formatCo2 } from '@/utils/format'

const auth = useAuthStore()
const gamification = useGamificationStore()

// Stage 7 fills these from GamificationService.
const modules = [
  {
    to: 'trips',
    label: 'Совместные поездки',
    text: 'Найдите попутчиков до кампуса и сократите выбросы.',
  },
  {
    to: 'eco-map',
    label: 'Переработка отходов',
    text: 'Карта контейнеров и сдача отходов по QR-коду.',
  },
  {
    to: 'vacancies',
    label: 'Карьера',
    text: 'Вакансии партнёров, которые смотрят на ваш ESG-профиль.',
  },
]
</script>

<template>
  <div class="stack">
    <section>
      <p class="eyebrow">Ваша активность</p>
      <h1>Здравствуйте, {{ auth.user?.firstName ?? 'студент' }}</h1>
    </section>

    <section class="stats">
      <article class="card stat">
        <p class="text-muted">EcoCoins</p>
        <p class="metric stat__value">{{ formatNumber(gamification.ecoCoins) }}</p>
      </article>
      <article class="card stat">
        <p class="text-muted">ESG-рейтинг</p>
        <p class="metric stat__value">{{ formatNumber(gamification.esgRating) }}</p>
      </article>
      <article class="card stat">
        <p class="text-muted">Сокращено выбросов</p>
        <p class="metric stat__value">{{ formatCo2(gamification.co2Saved) }}</p>
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
.stats,
.modules {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--space-4);
}

.stat__value {
  font-size: var(--text-2xl);
  font-weight: 600;
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
