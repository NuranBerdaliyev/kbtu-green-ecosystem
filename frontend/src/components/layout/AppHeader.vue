<script setup>
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useGamificationStore } from '@/stores/gamification'
import { formatNumber } from '@/utils/format'
import { ROLES } from '@/utils/constants'

const auth = useAuthStore()
const gamification = useGamificationStore()
const router = useRouter()

const links = [
  { name: 'home', label: 'Главная' },
  { name: 'trips', label: 'Поездки' },
  { name: 'eco-map', label: 'Контейнеры' },
  { name: 'vacancies', label: 'Вакансии' },
  { name: 'leaderboard', label: 'Рейтинг' },
]

async function signOut() {
  await auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <header class="header">
    <div class="header__inner container">
      <RouterLink :to="{ name: 'home' }" class="brand">
        <span class="brand__mark" aria-hidden="true" />
        KBTU Green
      </RouterLink>

      <nav class="nav">
        <RouterLink v-for="link in links" :key="link.name" :to="{ name: link.name }">
          {{ link.label }}
        </RouterLink>
        <RouterLink v-if="auth.hasRole(ROLES.ADMIN)" :to="{ name: 'admin' }">Админ</RouterLink>
      </nav>

      <div class="header__side">
        <span class="coins" title="EcoCoins">
          <span class="metric">{{ formatNumber(gamification.ecoCoins) }}</span>
          EC
        </span>
        <RouterLink :to="{ name: 'profile' }" class="profile-link">
          {{ auth.user?.firstName ?? 'Профиль' }}
        </RouterLink>
        <button class="signout" type="button" @click="signOut">Выйти</button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: var(--c-surface);
  border-bottom: 1px solid var(--c-line);
}

.header__inner {
  display: flex;
  align-items: center;
  gap: var(--space-5);
  min-height: var(--header-height);
}

.brand {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-family: var(--font-display);
  font-weight: 700;
  letter-spacing: -0.02em;
  white-space: nowrap;
}

.brand__mark {
  width: 12px;
  height: 12px;
  border-radius: 50% 0 50% 50%;
  background: var(--c-moss);
}

.nav {
  display: flex;
  gap: var(--space-4);
  flex: 1;
  font-size: var(--text-sm);
  overflow-x: auto;
}

.nav a {
  padding-block: var(--space-2);
  color: var(--c-ink-soft);
  white-space: nowrap;
  border-bottom: 2px solid transparent;
}

.nav a:hover {
  color: var(--c-ink);
}

.nav a.router-link-active {
  color: var(--c-moss);
  border-bottom-color: var(--c-moss);
}

.header__side {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  font-size: var(--text-sm);
}

.coins {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  background: var(--c-coin-soft);
  color: var(--c-coin);
  border-radius: 999px;
  font-weight: 600;
}

.signout {
  background: none;
  border: none;
  padding: 0;
  color: var(--c-ink-muted);
  cursor: pointer;
}

.signout:hover {
  color: var(--c-danger);
}

@media (max-width: 860px) {
  .header__inner {
    flex-wrap: wrap;
    padding-block: var(--space-3);
  }

  .nav {
    order: 3;
    width: 100%;
  }
}
</style>
