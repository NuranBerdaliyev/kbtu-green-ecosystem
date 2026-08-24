import http from './http'

/** VacancyController — /api/career/vacancies (GET returns a Spring Page). */
export const vacanciesApi = {
  search: (params) => http.get('/career/vacancies', { params }).then((r) => r.data),
  findById: (id) => http.get(`/career/vacancies/${id}`).then((r) => r.data),
  myVacancies: () => http.get('/career/vacancies/my').then((r) => r.data),
  create: (payload) => http.post('/career/vacancies', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/career/vacancies/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/career/vacancies/${id}`),
}

/** CompanyController — /api/career/companies */
export const companiesApi = {
  findAll: () => http.get('/career/companies').then((r) => r.data),
  findById: (id) => http.get(`/career/companies/${id}`).then((r) => r.data),
  myCompanies: () => http.get('/career/companies/my').then((r) => r.data),
  create: (payload) => http.post('/career/companies', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/career/companies/${id}`, payload).then((r) => r.data),
  setPartnerStatus: (id, isPartner) =>
    http.patch(`/career/companies/${id}/partner-status`, { isPartner }).then((r) => r.data),
  remove: (id) => http.delete(`/career/companies/${id}`),
}

/** JobApplicationController — /api/career */
export const applicationsApi = {
  apply: (vacancyId, coverLetter) =>
    http.post(`/career/vacancies/${vacancyId}/applications`, { coverLetter }).then((r) => r.data),
  myApplications: () => http.get('/career/applications/my').then((r) => r.data),
  /** HR view: candidates sorted by ESG_DESC or APPLIED_AT_DESC. */
  candidates: (vacancyId, sort = 'ESG_DESC') =>
    http
      .get(`/career/vacancies/${vacancyId}/applications`, { params: { sort } })
      .then((r) => r.data),
  changeStatus: (applicationId, status) =>
    http.patch(`/career/applications/${applicationId}/status`, { status }).then((r) => r.data),
}
