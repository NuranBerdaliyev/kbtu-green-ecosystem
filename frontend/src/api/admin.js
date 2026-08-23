import http from './http'

/** AdminController — /api/admin (ADMIN only). */
export const adminApi = {
  dashboard: () => http.get('/admin/dashboard').then((r) => r.data),
  vacancies: (page = 0, size = 20) =>
    http.get('/admin/vacancies', { params: { page, size } }).then((r) => r.data),
  setVacancyStatus: (vacancyId, isActive) =>
    http.patch(`/admin/vacancies/${vacancyId}/status`, { isActive }).then((r) => r.data),
}

/** UserController — /api/users (ADMIN only). */
export const usersAdminApi = {
  findAll: () => http.get('/users').then((r) => r.data),
  findById: (id) => http.get(`/users/${id}`).then((r) => r.data),
  update: (id, payload) => http.put(`/users/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/users/${id}`),
}
