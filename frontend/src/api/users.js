import http from './http'

/**
 * UserController — /api/users
 * UserResponseDto: { id, email, fullName, role, ecoCoinsBalance, esgRating,
 *                    totalCo2Saved, createdAt }
 *
 * Note: there is no GET /users/me yet. Until Backend №1 adds it in stage 3,
 * the auth store resolves the current user by id.
 */
export const usersApi = {
  findAll: () => http.get('/users').then((r) => r.data),
  findById: (id) => http.get(`/users/${id}`).then((r) => r.data),
  create: (payload) => http.post('/users', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/users/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/users/${id}`),
}
