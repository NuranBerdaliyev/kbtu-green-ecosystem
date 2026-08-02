import http from './http'

// Paths are placeholders until the API contract is fixed in stage 2.
export const authApi = {
  register: (payload) => http.post('/auth/register', payload).then((r) => r.data),
  login: (credentials) => http.post('/auth/login', credentials).then((r) => r.data),
  logout: () => http.post('/auth/logout'),
  me: () => http.get('/users/me').then((r) => r.data),
}
