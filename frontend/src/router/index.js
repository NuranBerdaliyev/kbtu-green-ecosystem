import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: (to, from, saved) => saved ?? { top: 0 },
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  // Rehydrate the session once after a hard refresh.
  await auth.restoreSession()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'home' }
  }

  // Role is read from the JWT identity; a role changed by an admin only takes
  // effect after the user signs in again.
  if (to.meta.roles?.length && !to.meta.roles.includes(auth.role)) {
    return { name: 'home', query: { forbidden: to.name } }
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} — KBTU Green` : 'KBTU Green Ecosystem'
})

export default router
