import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import { gamificationApi } from '@/api/gamification'
import { tokenStorage } from '@/utils/tokenStorage'

/**
 * There is no /auth/me endpoint. Identity (userId, email, role) comes from
 * AuthResponseDto and is cached in localStorage; the display name and the
 * EcoCoins / ESG / CO₂ figures come from GET /api/gamification/me.
 */
export const useAuthStore = defineStore('auth', () => {
  const identity = ref(tokenStorage.getIdentity())
  const stats = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const fieldErrors = ref({})

  const isAuthenticated = computed(() => Boolean(tokenStorage.getAccess() && identity.value))
  const role = computed(() => identity.value?.role ?? null)
  const userId = computed(() => identity.value?.userId ?? null)
  const fullName = computed(() => stats.value?.fullName ?? '')
  const firstName = computed(() => fullName.value.split(' ')[0] ?? '')
  const hasRole = (...allowed) => allowed.includes(role.value)

  function captureError(e) {
    error.value = e.message
    fieldErrors.value = e.fieldErrors ?? {}
  }

  function reset() {
    error.value = null
    fieldErrors.value = {}
  }

  async function loadStats() {
    try {
      stats.value = await gamificationApi.me()
    } catch {
      // A failed stats call must not block the app; the header just shows 0.
    }
  }

  async function login(credentials) {
    loading.value = true
    reset()
    try {
      const auth = await authApi.login(credentials)
      tokenStorage.set(auth)
      identity.value = tokenStorage.getIdentity()
      await loadStats()
      return true
    } catch (e) {
      captureError(e)
      return false
    } finally {
      loading.value = false
    }
  }

  /** Register also returns tokens, so the user lands signed in. */
  async function register(payload) {
    loading.value = true
    reset()
    try {
      const auth = await authApi.register(payload)
      tokenStorage.set(auth)
      identity.value = tokenStorage.getIdentity()
      await loadStats()
      return true
    } catch (e) {
      captureError(e)
      return false
    } finally {
      loading.value = false
    }
  }

  async function restoreSession() {
    if (!tokenStorage.getAccess()) return
    identity.value ??= tokenStorage.getIdentity()
    if (!stats.value) await loadStats()
  }

  function logout() {
    // The backend has no /auth/logout — refresh tokens are dropped client-side.
    tokenStorage.clear()
    identity.value = null
    stats.value = null
  }

  return {
    identity,
    stats,
    loading,
    error,
    fieldErrors,
    isAuthenticated,
    role,
    userId,
    fullName,
    firstName,
    hasRole,
    login,
    register,
    logout,
    loadStats,
    restoreSession,
  }
})
