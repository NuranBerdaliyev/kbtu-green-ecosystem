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
├── api/            one file per backend area; nothing else calls axios
├── assets/         base.css holds every design token
├── components/
│   ├── common/     BaseButton, BaseInput, BaseTextarea, StateBlock,
│   │               StatusBadge, FullnessBar, PageHeader
│   ├── carpool/    TripCard
│   ├── layout/     AppHeader
│   └── map/        LeafletMap
├── composables/    useAsync, useEcoContainerSocket
├── layouts/        DefaultLayout (app shell), AuthLayout (login/register)
├── router/         routes.js (route table) + index.js (guards)
├── stores/         Pinia — auth
├── utils/          tokenStorage, constants, geo, format
└── views/          one folder per module, mirrors the routes
```

## API contract

Verified against the controllers on `main`. **Three different prefixes** — the
axios layer has one client per prefix, see `src/api/http.js`.

| Area             | Base path                  | File                      |
| ---------------- | -------------------------- | ------------------------- |
| Auth (permitAll) | `/auth`                    | `src/api/auth.js`         |
| Profile          | `/profiles`                | `src/api/profile.js`      |
| Carpool          | `/api/carpool/trips`       | `src/api/trips.js`        |
| Eco Waste        | `/api/eco-points`          | `src/api/ecoWaste.js`     |
| Career           | `/api/career/*`            | `src/api/career.js`       |
| Gamification     | `/api/gamification`        | `src/api/gamification.js` |
| Admin            | `/api/admin`, `/api/users` | `src/api/admin.js`        |

The backend listens on **port 65535** (`application-dev.yaml`) and has no CORS
configuration, so the Vite proxy is required.

### WebSocket

SockJS + STOMP at `/ws-green`. Topics: `/topic/eco-containers` (updated
container after each deposit) and `/topic/admin/alerts` (string, fires past
90% full). Wrapped in `src/composables/useEcoContainerSocket.js`.

### Conventions

- `useAsync()` gives every screen loading / error / empty / data without
  try-catch in the view. Pair it with `StateBlock`.
- Geometry is WKT `POINT(lon lat)` — longitude first. Always use
  `src/utils/geo.js`.
- Spring `Page` responses are `{ content, totalPages, number, ... }`; plain
  list endpoints return arrays. Check which one you are calling.

## Stage 2 design

See [`docs/STAGE-2-FRONTEND.md`](docs/STAGE-2-FRONTEND.md) for the navigation
map, screen specs, and the open questions for the backend team.
