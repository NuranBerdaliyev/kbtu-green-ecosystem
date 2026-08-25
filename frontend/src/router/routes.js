import { ROLES } from '@/utils/constants'

/**
 * Route meta:
 *   requiresAuth — redirect to /login when there is no session
 *   roles        — allowed roles; omit to allow any signed-in user
 *   title        — used for document.title and the page header
 */
export const routes = [
  /*
   * Order matters. Both blocks below are mounted at "/", and Vue Router
   * resolves ties by declaration order. With AuthLayout first, visiting "/"
   * matched the auth parent with no child route and rendered a blank panel.
   * DefaultLayout must therefore come first so "/" resolves to Home.
   */
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

      // Stage 6 — HR workspace
      {
        path: 'hr/company',
        name: 'my-company',
        component: () => import('@/views/hr/MyCompanyView.vue'),
        meta: { title: 'Мои компании', requiresAuth: true, roles: [ROLES.HR] },
      },
      {
        path: 'hr/vacancies',
        name: 'my-vacancies',
        component: () => import('@/views/hr/MyVacanciesView.vue'),
        meta: { title: 'Мои вакансии', requiresAuth: true, roles: [ROLES.HR] },
      },

      // Stage 8 — Admin
      {
        path: 'admin',
        name: 'admin',
        component: () => import('@/views/admin/AdminDashboardView.vue'),
        meta: { title: 'Админ-панель', requiresAuth: true, roles: [ROLES.ADMIN] },
      },
      {
        path: 'admin/containers',
        name: 'admin-containers',
        component: () => import('@/views/admin/AdminContainersView.vue'),
        meta: { title: 'Контейнеры', requiresAuth: true, roles: [ROLES.ADMIN] },
      },
      {
        path: 'admin/companies',
        name: 'admin-companies',
        component: () => import('@/views/admin/AdminCompaniesView.vue'),
        meta: { title: 'Компании', requiresAuth: true, roles: [ROLES.ADMIN] },
      },
      {
        path: 'admin/users',
        name: 'admin-users',
        component: () => import('@/views/admin/AdminUsersView.vue'),
        meta: { title: 'Пользователи', requiresAuth: true, roles: [ROLES.ADMIN] },
      },
      {
        path: 'admin/waste-logs',
        name: 'admin-waste-logs',
        component: () => import('@/views/admin/AdminWasteLogsView.vue'),
        meta: { title: 'Журнал отходов', requiresAuth: true, roles: [ROLES.ADMIN] },
      },
    ],
  },
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
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: 'Страница не найдена' },
  },
]
