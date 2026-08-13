import http from './http'

/**
 * EcoPointController — /api/eco-point-containers
 * EcoPointContainerResponseDto: { id, title, locationWkt, wasteType,
 *                                 fullnessPercentage, isActive, qrCodeToken }
 *
 * WasteLogController — /api/waste-logs
 * WasteLogResponseDto: { id, userId, ecoPointContainerId, scannedAt, ecoCoinsEarned }
 */
export const ecoPointsApi = {
  findAll: () => http.get('/eco-point-containers').then((r) => r.data),
  findById: (id) => http.get(`/eco-point-containers/${id}`).then((r) => r.data),
  create: (payload) => http.post('/eco-point-containers', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/eco-point-containers/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/eco-point-containers/${id}`),
}

export const wasteLogsApi = {
  findAll: () => http.get('/waste-logs').then((r) => r.data),
  findById: (id) => http.get(`/waste-logs/${id}`).then((r) => r.data),
  create: (payload) => http.post('/waste-logs', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/waste-logs/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/waste-logs/${id}`),
}
