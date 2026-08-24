import http from './http'

/**
 * EcoPointContainerActionController — /api/eco-points (any signed-in user)
 * EcoPointContainerController — /api/eco-point-containers (ADMIN only)
 * WasteLogController — /api/waste-logs (ADMIN only)
 */
export const ecoPointsApi = {
  /** Active containers with live fullness. */
  activeContainers: () => http.get('/eco-points').then((r) => r.data),
  /** { qrCodeToken, addedFullnessPercentage, wasteWeightGrams } -> WasteLogResponseDto */
  deposit: (payload) => http.post('/eco-points/deposit', payload).then((r) => r.data),
}

export const containersAdminApi = {
  findAll: () => http.get('/eco-point-containers').then((r) => r.data),
  findById: (id) => http.get(`/eco-point-containers/${id}`).then((r) => r.data),
  create: (payload) => http.post('/eco-point-containers', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/eco-point-containers/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/eco-point-containers/${id}`),
}

export const wasteLogsAdminApi = {
  findAll: () => http.get('/waste-logs').then((r) => r.data),
}
