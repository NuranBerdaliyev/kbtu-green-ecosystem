/**
 * Mirrors backend/green/src/main/java/com/example/green/domain/enums/*.
 * If an enum changes there, change it here in the same pull request.
 */

/** Role — note there is no ROLE_ prefix in the backend enum. */
export const ROLES = {
  STUDENT: 'STUDENT',
  EMPLOYEE: 'EMPLOYEE',
  HR: 'HR',
  ADMIN: 'ADMIN',
}

export const ROLE_LABELS = {
  STUDENT: 'Студент',
  EMPLOYEE: 'Сотрудник',
  HR: 'HR-менеджер',
  ADMIN: 'Администратор',
}

/** WasteType — four values only; there is no METAL in the schema. */
export const WASTE_TYPES = {
  PLASTIC: 'PLASTIC',
  BATTERY: 'BATTERY',
  PAPER: 'PAPER',
  GLASS: 'GLASS',
}

export const WASTE_TYPE_LABELS = {
  PLASTIC: 'Пластик',
  BATTERY: 'Батарейки',
  PAPER: 'Бумага',
  GLASS: 'Стекло',
}

/** Colour per waste type — used by map markers and bin badges. */
export const WASTE_TYPE_COLORS = {
  PLASTIC: '#2F6B4F',
  BATTERY: '#A63A30',
  PAPER: '#B5791A',
  GLASS: '#3A6B7E',
}

export const TRIP_STATUS = {
  CREATED: 'CREATED',
  ACTIVE: 'ACTIVE',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
}

export const TRIP_STATUS_LABELS = {
  CREATED: 'Создана',
  ACTIVE: 'В пути',
  COMPLETED: 'Завершена',
  CANCELLED: 'Отменена',
}

export const JOB_STATUS = {
  PENDING: 'PENDING',
  REVIEWED: 'REVIEWED',
  ACCEPTED: 'ACCEPTED',
  REJECTED: 'REJECTED',
}

export const JOB_STATUS_LABELS = {
  PENDING: 'На рассмотрении',
  REVIEWED: 'Просмотрен',
  ACCEPTED: 'Принят',
  REJECTED: 'Отклонён',
}

/** Backend constraint: esg_rating BETWEEN 0 AND 100. */
export const ESG_RATING_MAX = 100

/** Backend constraint: cover_letter length BETWEEN 10 AND 5000. */
export const COVER_LETTER_MIN = 10
export const COVER_LETTER_MAX = 5000

/** KBTU main campus — default map centre. */
export const CAMPUS_CENTER = { lat: 43.2364, lng: 76.9457 }
