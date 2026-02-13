<template>
  <div :id="mapId" :style="{ height: height + 'px', width: '100%' }"></div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { useParcelStore } from '../stores/parcel'

const props = defineProps({
  parcels: {
    type: Array,
    default: () => []
  },
  height: {
    type: Number,
    default: 600
  },
  center: {
    type: Array,
    default: () => [28.6139, 77.2090] // New Delhi
  },
  zoom: {
    type: Number,
    default: 12
  }
})

const parcelStore = useParcelStore()
const mapId = ref(`map-${Math.random().toString(36).substr(2, 9)}`)
const map = ref(null)
const markers = ref(new Map())

// Custom marker icons
const createCustomIcon = (status) => {
  const colors = {
    'IN_TRANSIT': '#3b82f6',
    'OUT_FOR_DELIVERY': '#f59e0b',
    'DELIVERED': '#10b981',
    'PENDING': '#94a3b8',
    'FAILED': '#ef4444'
  }
  
  const color = colors[status] || '#6366f1'
  
  return L.divIcon({
    className: 'custom-marker',
    html: `
      <div style="
        background: ${color};
        width: 24px;
        height: 24px;
        border-radius: 50%;
        border: 3px solid white;
        box-shadow: 0 2px 8px rgba(0,0,0,0.3);
        animation: markerPulse 2s infinite;
      "></div>
    `,
    iconSize: [24, 24],
    iconAnchor: [12, 12]
  })
}

const initializeMap = () => {
  map.value = L.map(mapId.value, {
    zoomControl: true,
    attributionControl: false
  }).setView(props.center, props.zoom)
  
  // Dark theme tile layer
  L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
    maxZoom: 19
  }).addTo(map.value)
  
  // Add custom CSS for marker animation
  if (!document.getElementById('marker-animation-style')) {
    const style = document.createElement('style')
    style.id = 'marker-animation-style'
    style.innerHTML = `
      @keyframes markerPulse {
        0%, 100% { transform: scale(1); opacity: 1; }
        50% { transform: scale(1.1); opacity: 0.8; }
      }
    `
    document.head.appendChild(style)
  }
}

const updateMarkers = () => {
  if (!map.value) return
  
  // Remove markers that no longer exist
  const currentParcelIds = new Set(props.parcels.map(p => p.id))
  for (const [id, marker] of markers.value.entries()) {
    if (!currentParcelIds.has(id)) {
      map.value.removeLayer(marker)
      markers.value.delete(id)
    }
  }
  
  // Add or update markers
  props.parcels.forEach(parcel => {
    if (!parcel.currentLocation || !parcel.currentLocation.coordinates) return
    
    const [lon, lat] = parcel.currentLocation.coordinates
    const latLng = [lat, lon]
    
    if (markers.value.has(parcel.id)) {
      // Update existing marker position with smooth animation
      const marker = markers.value.get(parcel.id)
      marker.setLatLng(latLng)
      marker.setIcon(createCustomIcon(parcel.status))
    } else {
      // Create new marker
      const marker = L.marker(latLng, {
        icon: createCustomIcon(parcel.status)
      }).addTo(map.value)
      
      // Popup with parcel details
      marker.bindPopup(`
        <div style="color: #0f172a; font-family: Inter, sans-serif;">
          <strong style="font-size: 14px;">${parcel.trackingNumber}</strong><br/>
          <span style="font-size: 12px; color: #64748b;">Status: ${parcel.status}</span><br/>
          <span style="font-size: 12px; color: #64748b;">Driver: ${parcel.assignedDriverId || 'N/A'}</span>
        </div>
      `)
      
      markers.value.set(parcel.id, marker)
    }
  })
}

// Watch for parcel updates
watch(() => props.parcels, () => {
  updateMarkers()
}, { deep: true })

// Watch for location updates from WebSocket
watch(() => parcelStore.locationUpdates, () => {
  updateMarkers()
}, { deep: true })

onMounted(() => {
  initializeMap()
  updateMarkers()
})

onUnmounted(() => {
  if (map.value) {
    map.value.remove()
  }
})
</script>

<style scoped>
/* Leaflet overrides for dark theme */
:deep(.leaflet-container) {
  background: #0f172a;
  font-family: var(--font-family);
}

:deep(.leaflet-popup-content-wrapper) {
  background: white;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xl);
}

:deep(.leaflet-popup-tip) {
  background: white;
}

:deep(.leaflet-control-zoom) {
  border: none !important;
  box-shadow: var(--shadow-md);
}

:deep(.leaflet-control-zoom a) {
  background: var(--bg-card) !important;
  color: var(--text-primary) !important;
  border: 1px solid var(--border-color) !important;
}

:deep(.leaflet-control-zoom a:hover) {
  background: var(--bg-hover) !important;
}
</style>
