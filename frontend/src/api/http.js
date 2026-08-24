import axios from 'axios'
import { tokenStorage } from '@/utils/tokenStorage'

/**
 * The backend uses two different path prefixes:
 *   /api/...   — most controllers
 *   /auth/...  — AuthController (also the only permitAll path)
 *   /profiles  — ProfileController
 * So we keep two clients. `http` covers /api, `rootHttp` covers the rest.
 */
const apiBase = import.meta.env.VITE_API_BASE_URL ?? '/api'
const rootBase = import.meta.env.VITE_ROOT_BASE_URL ?? ''

function createClient(baseURL) {
  return axios.create({ baseURL, timeout: 15000, headers: { 'Content-Type': 'application/json' } })
}

export const http = createClient(apiBase)
export const rootHttp = createClient(rootBase)

/** No interceptors — refreshing must not trigger itself. */
const plain = createClient(rootBase)

let refreshing = null

function forceLogout() {
  tokenStorage.clear()
  if (window.location.pathname !== '/login') window.location.assign('/login')
}

function attachToken(config) {
  const token = tokenStorage.getAccess()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
}

function normaliseError(error) {
  const { response } = error
  if (!response) return new Error('Сервер недоступен. Проверьте, что backend запущен.')

  // ApiErrorResponse: { timestamp, status, error, message, path, validationErrors }
  const body = response.data ?? {}
  return Object.assign(new Error(body.message ?? 'Что-то пошло не так. Попробуйте ещё раз.'), {
    status: response.status,
    fieldErrors: body.validationErrors ?? {},
  })
}

function installInterceptors(client) {
  client.interceptors.request.use(attachToken)

  client.interceptors.response.use(
    (response) => response,
    async (error) => {
      const { response, config } = error

      // Never try to refresh a failing auth call.
      const isAuthCall = config?.url?.startsWith('/auth')

      if (response?.status === 401 && config && !config._retried && !isAuthCall) {
        const refreshToken = tokenStorage.getRefresh()
        if (!refreshToken) {
          forceLogout()
          return Promise.reject(normaliseError(error))
        }

        config._retried = true
        // One shared refresh for every request that failed at the same moment.
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
          return client(config)
        } catch {
          return Promise.reject(normaliseError(error))
        }
      }

      return Promise.reject(normaliseError(error))
    },
  )
}

installInterceptors(http)
installInterceptors(rootHttp)

export default http
