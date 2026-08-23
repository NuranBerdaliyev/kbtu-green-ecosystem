/**
 * JWT storage. AuthResponseDto is the shape the backend returns from
 * /auth/login, /auth/register and /auth/refresh:
 *   { tokenType, accessToken, accessExpiresIn, refreshToken,
 *     refreshExpiresIn, userId, email, role }
 *
 * There is no /auth/me endpoint, so the identity fields from that response
 * are cached here too — otherwise a page refresh would lose the user's role.
 */
const ACCESS_KEY = 'kge.accessToken'
const REFRESH_KEY = 'kge.refreshToken'
const IDENTITY_KEY = 'kge.identity'

export const tokenStorage = {
  getAccess: () => localStorage.getItem(ACCESS_KEY),
  getRefresh: () => localStorage.getItem(REFRESH_KEY),

  getIdentity() {
    try {
      return JSON.parse(localStorage.getItem(IDENTITY_KEY)) ?? null
    } catch {
      return null
    }
  },

  set(auth) {
    if (auth.accessToken) localStorage.setItem(ACCESS_KEY, auth.accessToken)
    if (auth.refreshToken) localStorage.setItem(REFRESH_KEY, auth.refreshToken)
    if (auth.userId) {
      localStorage.setItem(
        IDENTITY_KEY,
        JSON.stringify({ userId: auth.userId, email: auth.email, role: auth.role }),
      )
    }
  },

  clear() {
    localStorage.removeItem(ACCESS_KEY)
    localStorage.removeItem(REFRESH_KEY)
    localStorage.removeItem(IDENTITY_KEY)
  },
}
