import http from './http'

/** Career Hub module — stage 6. */
export const careerApi = {
  listCompanies: () => http.get('/companies').then((r) => r.data),
  listVacancies: (params) => http.get('/vacancies', { params }).then((r) => r.data),
  getVacancy: (id) => http.get(`/vacancies/${id}`).then((r) => r.data),
  apply: (vacancyId, payload) =>
    http.post(`/vacancies/${vacancyId}/applications`, payload).then((r) => r.data),
}
