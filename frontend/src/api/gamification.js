import http from './http'

/**
 * GamificationController — /api/gamification
 * getMyProfile -> { userId, fullName, ecoCoinsBalance, esgRating,
 *                   totalCo2Saved, leaderboardRank, unlockedAchievements }
 * history / leaderboard return a Spring Page.
 */
export const gamificationApi = {
  me: () => http.get('/gamification/me').then((r) => r.data),
  history: (page = 0, size = 20) =>
    http.get('/gamification/me/history', { params: { page, size } }).then((r) => r.data),
  achievements: () => http.get('/gamification/me/achievements').then((r) => r.data),
  leaderboard: (page = 0, size = 20) =>
    http.get('/gamification/leaderboard', { params: { page, size } }).then((r) => r.data),
}
