import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { useAuthStore } from '@/stores/auth'
import { tokenStorage } from '@/utils/tokenStorage'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: (to, from, saved) => saved ?? { top: 0 },
})

router.beforeEach((to) => {
  /*
   * The guard reads localStorage directly rather than a computed on the store.
   * A navigation guard is not a reactive context, so depending on reactivity
   * here is fragile: right after login the store may still hand back a stale
   * value and bounce the user back to /login. localStorage is written
   * synchronously by tokenStorage.set(), so it is always current.
   */
  const identity = tokenStorage.getIdentity()
  const authenticated = Boolean(tokenStorage.getAccess() && identity)

  if (to.meta.requiresAuth && !authenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.guestOnly && authenticated) {
    return { name: 'home' }
  }

  // Role lives in the JWT identity; an admin-changed role applies on next login.
  if (to.meta.roles?.length && !to.meta.roles.includes(identity?.role)) {
    return { name: 'home', query: { forbidden: to.name } }
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} — KBTU Green` : 'KBTU Green Ecosystem'

  // Rehydrate the store after navigation is settled, never blocking it.
  const auth = useAuthStore()
  auth.restoreSession()
})

export default router
