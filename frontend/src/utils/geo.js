/**
 * The backend stores geometry as PostGIS Point(4326) and exposes it over JSON
 * as a WKT string: "POINT(lon lat)". Note the order — longitude first.
 * Leaflet and most map libraries want [lat, lng], so always convert here.
 */

const POINT_PATTERN = /^\s*POINT\s*\(\s*(-?\d+(?:\.\d+)?)\s+(-?\d+(?:\.\d+)?)\s*\)\s*$/i

/** "POINT(76.9457 43.2364)" -> { lat: 43.2364, lng: 76.9457 } | null */
export function parseWkt(wkt) {
  const match = POINT_PATTERN.exec(wkt ?? '')
  if (!match) return null
  return { lng: Number(match[1]), lat: Number(match[2]) }
}

/** { lat, lng } -> "POINT(76.945700 43.236400)" */
export function toWkt({ lat, lng }) {
  return `POINT(${Number(lng).toFixed(6)} ${Number(lat).toFixed(6)})`
}

/** Straight-line distance in km. Good enough for a rough CO₂ estimate. */
export function haversineKm(a, b) {
  const R = 6371
  const toRad = (deg) => (deg * Math.PI) / 180
  const dLat = toRad(b.lat - a.lat)
  const dLng = toRad(b.lng - a.lng)
  const h =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(a.lat)) * Math.cos(toRad(b.lat)) * Math.sin(dLng / 2) ** 2
  return 2 * R * Math.asin(Math.sqrt(h))
}
