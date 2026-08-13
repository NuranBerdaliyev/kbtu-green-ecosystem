import http from './http'

/**
 * STAGE 3 — NONE OF THESE ENDPOINTS EXIST YET.
 *
 * The backend has jjwt on the classpath but no AuthController and no
 * SecurityConfig. These paths are the contract the frontend expects;
 * confirm them with Backend Developer №1 before stage 3 starts.
 *
 *   POST /api/auth/register  { email, fullName, password }  -> 201
 *   POST /api/auth/login     { email, password }
 *        -> { accessToken, refreshToken, user: UserResponseDto }
 *   POST /api/auth/refresh   { refreshToken } -> { accessToken, refreshToken }
 *   GET  /api/auth/me        -> UserResponseDto
 */
export const authApi = {
  register: (payload) => http.post('/auth/register', payload).then((r) => r.data),
  login: (credentials) => http.post('/auth/login', credentials).then((r) => r.data),
  logout: () => http.post('/auth/logout'),
  me: () => http.get('/auth/me').then((r) => r.data),
}
