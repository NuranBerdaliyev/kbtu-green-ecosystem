<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { CAMPUS_CENTER } from '@/utils/constants'

/**
 * Thin Leaflet wrapper. Markers are plain circles, so the default icon
 * assets (which Vite would need extra config to resolve) are never loaded.
 *
 * markers: [{ id, lat, lng, color, title, selected }]
 */
const props = defineProps({
  markers: { type: Array, default: () => [] },
  center: { type: Object, default: () => CAMPUS_CENTER },
  zoom: { type: Number, default: 14 },
  pickable: { type: Boolean, default: false },
  height: { type: String, default: '420px' },
})

const emit = defineEmits(['select', 'pick'])

const el = ref(null)
let map = null
let layer = null
let pickMarker = null

function draw() {
  if (!map) return
  layer.clearLayers()

  for (const marker of props.markers) {
    if (marker.lat == null || marker.lng == null) continue
    L.circleMarker([marker.lat, marker.lng], {
      radius: marker.selected ? 11 : 8,
      color: marker.color ?? '#2F6B4F',
      fillColor: marker.color ?? '#2F6B4F',
      fillOpacity: marker.selected ? 1 : 0.75,
      weight: marker.selected ? 3 : 2,
    })
      .bindTooltip(marker.title ?? '', { direction: 'top' })
      .on('click', () => emit('select', marker.id))
      .addTo(layer)
  }
}

onMounted(() => {
  map = L.map(el.value, { scrollWheelZoom: true }).setView(
    [props.center.lat, props.center.lng],
    props.zoom,
  )

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap',
    maxZoom: 19,
  }).addTo(map)

  layer = L.layerGroup().addTo(map)
  draw()

  if (props.pickable) {
    map.on('click', (event) => {
      const { lat, lng } = event.latlng
      pickMarker?.remove()
      pickMarker = L.circleMarker([lat, lng], {
        radius: 9,
        color: '#B5791A',
        fillColor: '#B5791A',
        fillOpacity: 1,
      }).addTo(map)
      emit('pick', { lat, lng })
    })
  }
})

onUnmounted(() => map?.remove())

watch(() => props.markers, draw, { deep: true })
</script>

<template>
  <div ref="el" class="map" :style="{ height }" />
</template>

<style scoped>
.map {
  width: 100%;
  border: 1px solid var(--c-line);
  border-radius: var(--radius);
  z-index: 0;
}
</style>
