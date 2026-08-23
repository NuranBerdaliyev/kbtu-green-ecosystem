import { rootHttp } from './http'

/** ProfileController — /profiles (no /api prefix). */
export const profileApi = {
  me: () => rootHttp.get('/profiles/me').then((r) => r.data),
  updateMe: (payload) => rootHttp.put('/profiles/me', payload).then((r) => r.data),
}
