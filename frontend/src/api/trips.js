import http from './http'

/**
 * TripController — /api/trips
 * TripResponseDto: { id, driverId, departureLocationWkt, departureTime,
 *                    totalSeats, availableSeats, tripStatus }
 *
 * TripParticipantController — /api/trip-participants
 * TripParticipantResponseDto: { id, tripId, passengerId, joinedAt, isCancelled }
 *
 * Joining a trip is currently "create a participant row" rather than
 * POST /trips/{id}/join. Business rules (seat count, duplicate join) arrive
 * in stage 4 — see the open questions in README.
 */
export const tripsApi = {
  findAll: () => http.get('/trips').then((r) => r.data),
  findById: (id) => http.get(`/trips/${id}`).then((r) => r.data),
  create: (payload) => http.post('/trips', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/trips/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/trips/${id}`),
}

export const tripParticipantsApi = {
  findAll: () => http.get('/trip-participants').then((r) => r.data),
  findById: (id) => http.get(`/trip-participants/${id}`).then((r) => r.data),
  create: (payload) => http.post('/trip-participants', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/trip-participants/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/trip-participants/${id}`),
}
