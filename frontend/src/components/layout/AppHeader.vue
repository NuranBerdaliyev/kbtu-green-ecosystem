<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { formatNumber } from '@/utils/format'
import { ROLES, ROLE_LABELS } from '@/utils/constants'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

/** Navigation is role-aware: HR and ADMIN get their own sections. */
const links = computed(() => {
  const base = [
    { name: 'home', label: 'Главная' },
    { name: 'trips', label: 'Поездки' },
    { name: 'eco-map', label: 'Контейнеры' },
    { name: 'vacancies', label: 'Вакансии' },
    { name: 'companies', label: 'Компании' },
    { name: 'leaderboard', label: 'Рейтинг' },
  ]

  if (auth.hasRole(ROLES.HR)) {
    base.push({ name: 'my-company', label: 'Мои компании' })
    base.push({ name: 'my-vacancies', label: 'Мои вакансии' })
  }
  if (auth.hasRole(ROLES.ADMIN)) {
    base.push({ name: 'admin', label: 'Админ' })
  }
  return base
})

/**
 * router-link-active matches by prefix, and every path starts with "/",
 * so the Home link would stay lit everywhere. Home is matched exactly;
 * the rest match by prefix so /trips/5 still highlights "Поездки".
 */
function isActive(link) {
  const target = router.resolve({ name: link.name }).path
  return target === '/' ? route.path === '/' : route.path.startsWith(target)
}

function signOut() {
  auth.logout()
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
        <RouterLink
          v-for="link in links"
          :key="link.name"
          :to="{ name: link.name }"
          :class="{ 'is-active': isActive(link) }"
        >
          {{ link.label }}
        </RouterLink>
      </nav>

      <div class="header__side">
        <RouterLink :to="{ name: 'profile' }" class="coins" title="EcoCoins">
          <span class="metric">{{ formatNumber(auth.stats?.ecoCoinsBalance) }}</span>
          EC
        </RouterLink>
        <RouterLink :to="{ name: 'profile' }" class="profile-link">
          <span class="profile-link__name">{{ auth.firstName || 'Профиль' }}</span>
          <span class="profile-link__role">{{ ROLE_LABELS[auth.role] ?? '' }}</span>
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

.nav a.is-active {
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

.profile-link {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.profile-link__role {
  font-size: var(--text-xs);
  color: var(--c-ink-muted);
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