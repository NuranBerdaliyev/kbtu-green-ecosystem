const numberFormat = new Intl.NumberFormat('ru-RU')

/** 1250 -> "1 250" */
export const formatNumber = (value) => numberFormat.format(value ?? 0)

/** 12.456 -> "12.46 кг" */
export const formatCo2 = (kg) => `${(kg ?? 0).toFixed(2)} кг CO₂`

/** ISO string -> "2 авг, 14:30" */
export const formatDateTime = (iso) =>
  iso
    ? new Date(iso).toLocaleString('ru-RU', {
        day: 'numeric',
        month: 'short',
        hour: '2-digit',
        minute: '2-digit',
      })
    : ''
