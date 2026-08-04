import { ref } from 'vue'
import { defineStore } from 'pinia'
import { gamificationApi } from '@/api/gamification'

/**
 * EcoCoins / ESG state, shown in the header on every page.
 * Filled in during stage 7 — the API is stubbed until then.
 */
export const useGamificationStore = defineStore('gamification', () => {
  const ecoCoins = ref(0)
  const esgRating = ref(0)
  const co2Saved = ref(0)
  const loading = ref(false)

  async function loadStats() {
    loading.value = true
    try {
      const stats = await gamificationApi.myStats()
      ecoCoins.value = stats.ecoCoins ?? 0
      esgRating.value = stats.esgRating ?? 0
      co2Saved.value = stats.co2Saved ?? 0
    } finally {
      loading.value = false
    }
  }

  return { ecoCoins, esgRating, co2Saved, loading, loadStats }
})
