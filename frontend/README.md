# KBTU Green Ecosystem — Frontend

Vue 3 + Vite SPA for the KBTU Green Ecosystem platform.

## Requirements

- Node.js 22.18+ (or 24.12+) — check with `node -v`
- Backend running on `http://localhost:8080`
- PostgreSQL from `backend/docker-compose.yml`

## Getting started

```bash
cd frontend
npm install
npm run dev          # http://localhost:5173
```

The dev server proxies `/api` and `/ws` to `http://localhost:8080`, so there is
no CORS setup to do while developing. The proxy lives in `vite.config.js`.

## Scripts

| Command           | What it does                       |
| ----------------- | ---------------------------------- |
| `npm run dev`     | Dev server with hot reload         |
| `npm run build`   | Production build into `dist/`      |
| `npm run preview` | Serve the production build locally |
| `npm run lint`    | ESLint with autofix                |
| `npm run format`  | Prettier over `src/`               |

## Project structure

```
src/
├── api/            HTTP layer — one file per backend module
│   ├── http.js     axios instance, JWT header, refresh on 401
│   ├── auth.js     login / register / me
│   ├── trips.js    carpool (stage 4)
│   ├── ecoWaste.js bins and deposits (stage 5)
│   ├── career.js   companies and vacancies (stage 6)
│   └── gamification.js  EcoCoins, ESG, leaderboard (stage 7)
├── assets/         base.css holds every design token
├── components/
│   ├── common/     BaseButton, BaseInput, StagePlaceholder
│   └── layout/     AppHeader
├── composables/    reusable logic (useMap, useWebSocket, …)
├── layouts/        DefaultLayout (app shell), AuthLayout (login/register)
├── router/         routes.js (route table) + index.js (guards)
├── stores/         Pinia — auth, gamification
├── utils/          tokenStorage, constants, format
└── views/          one folder per module, mirrors the routes
```

## Conventions

- **No component talks to axios directly.** Views call a store or an `api/*`
  function. This keeps the API contract in one place when the backend changes.
- **Tokens live only in `utils/tokenStorage.js`.** Nothing else touches
  `localStorage`.
- **No hard-coded colours or fonts.** Use the CSS variables in
  `assets/base.css`; if a token is missing, add it there.
- **Numbers use `.metric`** so EcoCoins and CO₂ values line up in columns.
- Routes are named — link with `:to="{ name: 'trips' }"`, never a raw string.
- Route access is declared in `router/routes.js` meta: `requiresAuth`, `roles`.

## Environment variables

`.env.development` and `.env.production` are committed. Put personal overrides
in `.env.local` (git-ignored).

| Variable            | Purpose                                               |
| ------------------- | ----------------------------------------------------- |
| `VITE_API_BASE_URL` | REST base URL (`/api` in dev, full URL in production) |
| `VITE_WS_URL`       | WebSocket endpoint used by the Eco Waste module       |

## Open questions for the backend team (stage 2)

1. Are controllers served under `/api/**`, or should the Vite proxy strip the
   prefix?
2. Login response shape — expected `{ accessToken, refreshToken }`.
3. Refresh endpoint — expected `POST /auth/refresh` with `{ refreshToken }`.
4. Current user endpoint — expected `GET /users/me` returning
   `{ id, firstName, lastName, email, roles: [] }`.
5. Role names — the frontend assumes `ROLE_STUDENT`, `ROLE_STAFF`, `ROLE_HR`,
   `ROLE_ADMIN` (see `utils/constants.js`).
6. Error response shape — the axios layer reads `response.data.message`.
