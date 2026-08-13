import http from './http'

/**
 * VacancyController — /api/vacancies
 * VacancyResponseDto: { id, hrManagerId, companyName, title, description,
 *                       isPartnerVacancy }
 *
 * JobApplicationController — /api/job-applications
 * JobApplicationResponseDto: { id, vacancyId, studentId, appliedAt,
 *                              coverLetter, jobStatus }
 *
 * There is no Company entity — companyName is a plain string on the vacancy.
 * The Companies page groups vacancies by that string; see groupByCompany below.
 */
export const vacanciesApi = {
  findAll: () => http.get('/vacancies').then((r) => r.data),
  findById: (id) => http.get(`/vacancies/${id}`).then((r) => r.data),
  create: (payload) => http.post('/vacancies', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/vacancies/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/vacancies/${id}`),
}

export const jobApplicationsApi = {
  findAll: () => http.get('/job-applications').then((r) => r.data),
  findById: (id) => http.get(`/job-applications/${id}`).then((r) => r.data),
  create: (payload) => http.post('/job-applications', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/job-applications/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/job-applications/${id}`),
}

/** Derives a company list from vacancies until a Company entity exists. */
export function groupByCompany(vacancies) {
  const companies = new Map()
  for (const vacancy of vacancies) {
    const entry = companies.get(vacancy.companyName) ?? {
      name: vacancy.companyName,
      isPartner: false,
      vacancies: [],
    }
    entry.vacancies.push(vacancy)
    entry.isPartner ||= Boolean(vacancy.isPartnerVacancy)
    companies.set(vacancy.companyName, entry)
  }
  return [...companies.values()].sort((a, b) => a.name.localeCompare(b.name, 'ru'))
}
