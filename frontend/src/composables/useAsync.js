import { ref, shallowRef } from 'vue'

/**
 * Wraps an async call in the four states every list screen needs:
 * loading / error / empty / data. Keeps views free of try-catch noise.
 *
 *   const trips = useAsync(() => tripsApi.search(params), [])
 *   onMounted(trips.run)
 */
export function useAsync(fn, initial = null) {
  const data = shallowRef(initial)
  const loading = ref(false)
  const error = ref(null)

  async function run(...args) {
    loading.value = true
    error.value = null
    try {
      data.value = await fn(...args)
      return data.value
    } catch (e) {
      error.value = e.message
      return null
    } finally {
      loading.value = false
    }
  }

  return { data, loading, error, run }
}
