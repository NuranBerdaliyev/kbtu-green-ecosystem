import http from './http'

/** Carpool module — stage 4. */
export const tripsApi = {
  search: (params) => http.get('/trips', { params }).then((r) => r.data),
  getById: (id) => http.get(`/trips/${id}`).then((r) => r.data),
  create: (payload) => http.post('/trips', payload).then((r) => r.data),
  join: (id) => http.post(`/trips/${id}/join`).then((r) => r.data),
  complete: (id) => http.post(`/trips/${id}/complete`).then((r) => r.data),
}
