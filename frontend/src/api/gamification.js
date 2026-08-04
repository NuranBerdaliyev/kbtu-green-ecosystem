import http from './http'

/** Gamification engine — stage 7. */
export const gamificationApi = {
  myStats: () => http.get('/gamification/me').then((r) => r.data),
  achievements: () => http.get('/gamification/achievements').then((r) => r.data),
  leaderboard: (params) => http.get('/gamification/leaderboard', { params }).then((r) => r.data),
  history: (params) => http.get('/gamification/history', { params }).then((r) => r.data),
}
