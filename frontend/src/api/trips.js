import http from './http'

/**
 * TripController + TripParticipantController — /api/carpool/trips
 * The driver comes from the JWT, so create() does not send driverId.
 */
export const tripsApi = {
  search: (params) => http.get('/carpool/trips/search', { params }).then((r) => r.data),
  findById: (tripId) => http.get(`/carpool/trips/${tripId}`).then((r) => r.data),
  myTrips: () => http.get('/carpool/trips/my').then((r) => r.data),
  joinedTrips: () => http.get('/carpool/trips/joined').then((r) => r.data),

  create: (payload) => http.post('/carpool/trips', payload).then((r) => r.data),
  update: (tripId, payload) => http.put(`/carpool/trips/${tripId}`, payload).then((r) => r.data),
  remove: (tripId) => http.delete(`/carpool/trips/${tripId}`),

  activate: (tripId) => http.post(`/carpool/trips/${tripId}/activate`).then((r) => r.data),
  complete: (tripId) => http.post(`/carpool/trips/${tripId}/complete`).then((r) => r.data),
  cancel: (tripId) => http.post(`/carpool/trips/${tripId}/cancel`).then((r) => r.data),

  participants: (tripId) => http.get(`/carpool/trips/${tripId}/participants`).then((r) => r.data),
  join: (tripId) => http.post(`/carpool/trips/${tripId}/participants/join`).then((r) => r.data),
  leave: (tripId) => http.delete(`/carpool/trips/${tripId}/participants/leave`).then((r) => r.data),
}
