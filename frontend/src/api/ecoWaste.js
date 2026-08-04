import http from './http'

/** Eco Waste module — stage 5. */
export const ecoWasteApi = {
  listBins: (params) => http.get('/eco-bins', { params }).then((r) => r.data),
  getBin: (id) => http.get(`/eco-bins/${id}`).then((r) => r.data),
  deposit: (payload) => http.post('/deposits', payload).then((r) => r.data),
  myDeposits: () => http.get('/deposits/me').then((r) => r.data),
}
