import { ROLES } from './constants'

/**
 * Capability checks used by views and by the router.
 * Keeping them here stops each screen inventing its own role logic.
 */
export const can = {
  applyToVacancy: (role) => role === ROLES.STUDENT,
  manageOwnCompany: (role) => role === ROLES.HR,
  reviewCandidates: (role) => role === ROLES.HR,
  administer: (role) => role === ROLES.ADMIN,
  /** HR and ADMIN are excluded from the leaderboard by the backend. */
  appearInLeaderboard: (role) => role === ROLES.STUDENT || role === ROLES.EMPLOYEE,
}
