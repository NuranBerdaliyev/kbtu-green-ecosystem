import { ROLES } from '@/utils/constants'

/**
 * Route meta:
 *   requiresAuth — redirect to /login when there is no session
 *   roles        — allowed roles; omit to allow any signed-in user
 *   title        — used for document.title and the page header
 */
export const routes = [
  {
    path: '/',
    component: () => import('@/layouts/AuthLayout.vue'),
    children: [
      {
        path: 'login',
        name: 'login',
        component: () => import('@/views/auth/LoginView.vue'),
        meta: { title: 'Вход', guestOnly: true },
      },
      {
        path: 'register',
        name: 'register',
        component: () => import('@/views/auth/RegisterView.vue'),
        meta: { title: 'Регистрация', guestOnly: true },
      },
    ],
  },
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: 'Главная', requiresAuth: true },
      },

      // Stage 4 — Carpool
      {
        path: 'trips',
        name: 'trips',
        component: () => import('@/views/carpool/TripsView.vue'),
        meta: { title: 'Поездки', requiresAuth: true },
      },
      {
        path: 'trips/create',
        name: 'trip-create',
        component: () => import('@/views/carpool/CreateTripView.vue'),
        meta: { title: 'Новая поездка', requiresAuth: true },
      },
      {
        path: 'trips/:id',
        name: 'trip-details',
        component: () => import('@/views/carpool/TripDetailsView.vue'),
        meta: { title: 'Поездка', requiresAuth: true },
      },

      // Stage 5 — Eco Waste
      {
        path: 'eco-bins',
        name: 'eco-map',
        component: () => import('@/views/ecowaste/EcoMapView.vue'),
        meta: { title: 'Карта контейнеров', requiresAuth: true },
      },
      {
        path: 'eco-bins/:id',
        name: 'eco-bin',
        component: () => import('@/views/ecowaste/EcoBinView.vue'),
        meta: { title: 'Контейнер', requiresAuth: true },
      },
      {
        path: 'deposit',
        name: 'deposit',
        component: () => import('@/views/ecowaste/DepositView.vue'),
        meta: { title: 'Сдать отходы', requiresAuth: true },
      },

      // Stage 6 — Career Hub
      {
        path: 'vacancies',
        name: 'vacancies',
        component: () => import('@/views/career/VacanciesView.vue'),
        meta: { title: 'Вакансии', requiresAuth: true },
      },
      {
        path: 'vacancies/:id',
        name: 'vacancy-details',
        component: () => import('@/views/career/VacancyDetailsView.vue'),
        meta: { title: 'Вакансия', requiresAuth: true },
      },
      {
        path: 'companies',
        name: 'companies',
        component: () => import('@/views/career/CompaniesView.vue'),
        meta: { title: 'Компании', requiresAuth: true },
      },

      // Stage 7 — Gamification
      {
        path: 'leaderboard',
        name: 'leaderboard',
        component: () => import('@/views/profile/LeaderboardView.vue'),
        meta: { title: 'Рейтинг', requiresAuth: true },
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: { title: 'Профиль', requiresAuth: true },
      },
      {
        path: 'achievements',
        name: 'achievements',
        component: () => import('@/views/profile/AchievementsView.vue'),
        meta: { title: 'Достижения', requiresAuth: true },
      },

      // Stage 8 — Admin
      {
        path: 'admin',
        name: 'admin',
        component: () => import('@/views/admin/AdminDashboardView.vue'),
        meta: { title: 'Админ-панель', requiresAuth: true, roles: [ROLES.ADMIN] },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: 'Страница не найдена' },
  },
]
