import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import { tokenStorage } from '@/utils/tokenStorage'

/**
 * User shape comes from UserResponseDto:
 * { id, email, fullName, role, ecoCoinsBalance, esgRating, totalCo2Saved, createdAt }
 *
 * `role` is a single value, not an array — the backend enum has one role per user.
 */
export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const fieldErrors = ref({})

  const isAuthenticated = computed(() => Boolean(user.value ?? tokenStorage.getAccess()))
  const role = computed(() => user.value?.role ?? null)
  const hasRole = (...allowed) => allowed.includes(role.value)

  /** "Нуран Бердалиев" -> "Нуран" */
  const firstName = computed(() => user.value?.fullName?.split(' ')[0] ?? '')

  function captureError(e) {
    error.value = e.message
    fieldErrors.value = e.fieldErrors ?? {}
  }

  function resetErrors() {
    error.value = null
    fieldErrors.value = {}
  }

  async function login(credentials) {
    loading.value = true
    resetErrors()
    try {
      const data = await authApi.login(credentials)
      tokenStorage.set(data)
      // Stage 3: login is expected to return the user alongside the tokens.
      user.value = data.user ?? (await authApi.me())
      return true
    } catch (e) {
      captureError(e)
      return false
    } finally {
      loading.value = false
    }
  }

  async function register(payload) {
    loading.value = true
    resetErrors()
    try {
      await authApi.register(payload)
      return true
    } catch (e) {
      captureError(e)
      return false
    } finally {
      loading.value = false
    }
  }

  /** Called once on app start so a page refresh keeps the session. */
  async function restoreSession() {
    if (!tokenStorage.getAccess() || user.value) return
    try {
      user.value = await authApi.me()
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
    fieldErrors,
    isAuthenticated,
    role,
    firstName,
    hasRole,
    login,
    register,
    logout,
    restoreSession,
  }
})
