import http from './http'

/**
 * EcoPointContainerActionController — /api/eco-points (any signed-in user)
 * EcoPointContainerController — /api/eco-point-containers (ADMIN only)
 * WasteLogController — /api/waste-logs (ADMIN only)
 */
export const ecoPointsApi = {
  /** Active containers with live fullness. */
  activeContainers: () => http.get('/eco-points').then((r) => r.data),
  /**
   * { qrCodeToken, wasteWeightGrams } -> WasteLogResponseDto with status PENDING.
   * The container is not changed and nothing is awarded until an admin approves.
   */
  deposit: (payload) => http.post('/eco-points/deposit', payload).then((r) => r.data),
}

export const containersAdminApi = {
  findAll: () => http.get('/eco-point-containers').then((r) => r.data),
  findById: (id) => http.get(`/eco-point-containers/${id}`).then((r) => r.data),
  create: (payload) => http.post('/eco-point-containers', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/eco-point-containers/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/eco-point-containers/${id}`),
  /** Resets currentWeightGrams and fullness to 0 and broadcasts the change. */
  empty: (id) => http.post(`/eco-point-containers/${id}/empty`).then((r) => r.data),
}

export const wasteLogsAdminApi = {
  findAll: () => http.get('/waste-logs').then((r) => r.data),
  findPending: () => http.get('/waste-logs/pending').then((r) => r.data),
  findById: (id) => http.get(`/waste-logs/${id}`).then((r) => r.data),
  /** Applies the weight, recomputes fullness and grants the reward atomically. */
  approve: (id) => http.post(`/waste-logs/${id}/approve`).then((r) => r.data),
  reject: (id) => http.post(`/waste-logs/${id}/reject`).then((r) => r.data),
}
