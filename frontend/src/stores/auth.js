import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import { tokenStorage } from '@/utils/tokenStorage'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const loading = ref(false)
  const error = ref(null)

  const isAuthenticated = computed(() => Boolean(user.value ?? tokenStorage.getAccess()))
  const roles = computed(() => user.value?.roles ?? [])
  const hasRole = (role) => roles.value.includes(role)

  async function login(credentials) {
    loading.value = true
    error.value = null
    try {
      const tokens = await authApi.login(credentials)
      tokenStorage.set(tokens)
      await fetchCurrentUser()
      return true
    } catch (e) {
      error.value = e.message
      return false
    } finally {
      loading.value = false
    }
  }

  async function register(payload) {
    loading.value = true
    error.value = null
    try {
      await authApi.register(payload)
      return true
    } catch (e) {
      error.value = e.message
      return false
    } finally {
      loading.value = false
    }
  }

  async function fetchCurrentUser() {
    if (!tokenStorage.getAccess()) return null
    user.value = await authApi.me()
    return user.value
  }

  /** Called once on app start so a page refresh keeps the session. */
  async function restoreSession() {
    if (!tokenStorage.getAccess() || user.value) return
    try {
      await fetchCurrentUser()
    } catch {
      tokenStorage.clear()
      user.value = null
    }
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // Logging out locally matters more than the server call succeeding.
    }
    tokenStorage.clear()
    user.value = null
  }

  return {
    user,
    loading,
    error,
    isAuthenticated,
    roles,
    hasRole,
    login,
    register,
    logout,
    fetchCurrentUser,
    restoreSession,
  }
})
