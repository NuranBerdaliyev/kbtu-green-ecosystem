import { rootHttp } from './http'

/**
 * AuthController — /auth (no /api prefix, and the only permitAll path).
 * All three return AuthResponseDto.
 */
export const authApi = {
  register: (payload) => rootHttp.post('/auth/register', payload).then((r) => r.data),
  login: (credentials) => rootHttp.post('/auth/login', credentials).then((r) => r.data),
  refresh: (refreshToken) => rootHttp.post('/auth/refresh', { refreshToken }).then((r) => r.data),
}
