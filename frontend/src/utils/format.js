const numberFormat = new Intl.NumberFormat('ru-RU')

/** 1250 -> "1 250" */
export const formatNumber = (value) => numberFormat.format(Number(value ?? 0))

/** "12.456" -> "12.46 кг CO₂" (backend sends BigDecimal as a string) */
export const formatCo2 = (kg) => `${Number(kg ?? 0).toFixed(2)} кг CO₂`

/** 40 -> "+40", -5 -> "-5" */
export const formatDelta = (value) => {
  const n = Number(value ?? 0)
  return n > 0 ? `+${numberFormat.format(n)}` : numberFormat.format(n)
}

/** LocalDateTime "2026-08-18T08:30:00" -> "18 авг, 08:30" */
export const formatDateTime = (iso) =>
  iso
    ? new Date(iso).toLocaleString('ru-RU', {
        day: 'numeric',
        month: 'short',
        hour: '2-digit',
        minute: '2-digit',
      })
    : ''

/** "2026-08-18T08:30:00" -> "вт, 18 августа" */
export const formatDate = (iso) =>
  iso
    ? new Date(iso).toLocaleDateString('ru-RU', {
        weekday: 'short',
        day: 'numeric',
        month: 'long',
      })
    : ''

/** Value for <input type="datetime-local"> from a Date. */
export const toDateTimeLocal = (date) => {
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(
    date.getHours(),
  )}:${pad(date.getMinutes())}`
}

/**
 * The backend expects LocalDateTime without a timezone.
 * "2026-08-18T08:30" (input value) -> "2026-08-18T08:30:00"
 */
export const toLocalDateTime = (value) => (value?.length === 16 ? `${value}:00` : value)
