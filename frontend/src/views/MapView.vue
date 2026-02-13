<template>
  <div class="map-view">
    <Navbar />
    
    <div class="map-container-full">
      <div class="map-controls card-glass">
        <h3>Live Tracking</h3>
        <div class="control-group">
          <label>Filter by Status</label>
          <select v-model="selectedStatus" class="input">
            <option value="ALL">All Parcels</option>
            <option value="IN_TRANSIT">In Transit</option>
            <option value="OUT_FOR_DELIVERY">Out for Delivery</option>
            <option value="DELIVERED">Delivered</option>
          </select>
        </div>
        
        <div class="stats-mini">
          <div class="stat-mini">
            <span class="stat-mini-value">{{ filteredParcels.length }}</span>
            <span class="stat-mini-label">Visible</span>
          </div>
          <div class="stat-mini">
            <span class="stat-mini-value">{{ parcelStore.parcelCount }}</span>
            <span class="stat-mini-label">Total</span>
          </div>
        </div>
        
        <div class="legend">
          <h4>Legend</h4>
          <div class="legend-item">
            <span class="legend-dot" style="background: #3b82f6;"></span>
            <span>In Transit</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot" style="background: #f59e0b;"></span>
            <span>Out for Delivery</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot" style="background: #10b981;"></span>
            <span>Delivered</span>
          </div>
        </div>
      </div>
      
      <div class="map-wrapper">
        <LiveMap :parcels="filteredParcels" :height="mapHeight" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useParcelStore } from '../stores/parcel'
import Navbar from '../components/Navbar.vue'
import LiveMap from '../components/LiveMap.vue'

const parcelStore = useParcelStore()
const selectedStatus = ref('ALL')
const mapHeight = ref(window.innerHeight - 80)

const filteredParcels = computed(() => {
  if (selectedStatus.value === 'ALL') {
    return parcelStore.parcels
  }
  return parcelStore.parcels.filter(p => p.status === selectedStatus.value)
})

const handleResize = () => {
  mapHeight.value = window.innerHeight - 80
}

onMounted(async () => {
  await parcelStore.fetchParcels()
  if (!parcelStore.connected) {
    parcelStore.connectWebSocket()
  }
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.map-view {
  min-height: 100vh;
  background: var(--bg-primary);
}

.map-container-full {
  position: relative;
  height: calc(100vh - 80px);
}

.map-controls {
  position: absolute;
  top: var(--spacing-lg);
  left: var(--spacing-lg);
  z-index: 1000;
  width: 280px;
  padding: var(--spacing-lg);
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}

.map-controls h3 {
  margin-bottom: var(--spacing-lg);
}

.control-group {
  margin-bottom: var(--spacing-lg);
}

.control-group label {
  display: block;
  font-size: var(--font-size-sm);
  font-weight: 600;
  margin-bottom: var(--spacing-sm);
  color: var(--text-secondary);
}

.stats-mini {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.stat-mini {
  background: var(--bg-tertiary);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  text-align: center;
}

.stat-mini-value {
  display: block;
  font-size: var(--font-size-2xl);
  font-weight: 800;
  color: var(--color-primary);
}

.stat-mini-label {
  display: block;
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  text-transform: uppercase;
}

.legend h4 {
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-md);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
  font-size: var(--font-size-sm);
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
}

.map-wrapper {
  width: 100%;
  height: 100%;
}
</style>
