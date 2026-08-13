import axios from 'axios'
import { tokenStorage } from '@/utils/tokenStorage'

const baseURL = import.meta.env.VITE_API_BASE_URL ?? '/api'

/** Main client. Every request carries the access token if we have one. */
export const http = axios.create({
  baseURL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

/**
 * Bare client without interceptors — used only to refresh the token,
 * otherwise a failing refresh would trigger itself in a loop.
 */
const plain = axios.create({ baseURL, timeout: 15000 })

http.interceptors.request.use((config) => {
  const token = tokenStorage.getAccess()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

let refreshing = null

function forceLogout() {
  tokenStorage.clear()
  // Full reload on forced logout: clears every store at once.
  if (window.location.pathname !== '/login') {
    window.location.assign('/login')
  }
}

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { response, config } = error

    if (!response) {
      // Network error / backend not running.
      return Promise.reject(new Error('Сервер недоступен. Проверьте подключение.'))
    }

    if (response.status === 401 && !config._retried) {
      const refreshToken = tokenStorage.getRefresh()
      if (!refreshToken) {
        forceLogout()
        return Promise.reject(error)
      }

      config._retried = true
      // Share one refresh call between all requests that failed at the same time.
      refreshing ??= plain
        .post('/auth/refresh', { refreshToken })
        .then(({ data }) => {
          tokenStorage.set(data)
          return data.accessToken
        })
        .catch((err) => {
          forceLogout()
          throw err
        })
        .finally(() => {
          refreshing = null
        })

      try {
        const accessToken = await refreshing
        config.headers.Authorization = `Bearer ${accessToken}`
        return http(config)
      } catch {
        return Promise.reject(error)
      }
    }

    // Backend error shape (api/error/ApiErrorResponse.java):
    // { timestamp, status, error, message, path, validationErrors: { field: msg } }
    const body = response.data ?? {}
    const message = body.message ?? 'Что-то пошло не так. Попробуйте ещё раз.'
    return Promise.reject(
      Object.assign(new Error(message), {
        status: response.status,
        // Map field name -> message, so forms can show errors inline.
        fieldErrors: body.validationErrors ?? {},
      }),
    )
  },
)

export default http
