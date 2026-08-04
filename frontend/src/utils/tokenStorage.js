/**
 * Single place where JWT tokens live.
 * Both the axios layer and the auth store read/write through here,
 * so nothing else in the app touches localStorage directly.
 */
const ACCESS_KEY = 'kge.accessToken'
const REFRESH_KEY = 'kge.refreshToken'

export const tokenStorage = {
  getAccess: () => localStorage.getItem(ACCESS_KEY),
  getRefresh: () => localStorage.getItem(REFRESH_KEY),

  set({ accessToken, refreshToken }) {
    if (accessToken) localStorage.setItem(ACCESS_KEY, accessToken)
    if (refreshToken) localStorage.setItem(REFRESH_KEY, refreshToken)
  },

  clear() {
    localStorage.removeItem(ACCESS_KEY)
    localStorage.removeItem(REFRESH_KEY)
  },
}
