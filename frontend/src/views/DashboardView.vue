<template>
  <div class="dashboard">
    <Navbar />
    
    <div class="dashboard-content">
      <div class="dashboard-header">
        <h1>Command Center</h1>
        <div class="connection-status">
          <span class="status-dot" :class="{ connected: parcelStore.connected }"></span>
          <span>{{ parcelStore.connected ? 'Live' : 'Disconnected' }}</span>
        </div>
      </div>
      
      <!-- Stats Cards -->
      <div class="stats-grid grid grid-cols-4">
        <div class="stat-card card fade-in">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f59e0b, #d97706);">
            📦
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ parcelStore.stats.pending }}</div>
            <div class="stat-label">Pending</div>
          </div>
        </div>
        
        <div class="stat-card card fade-in" style="animation-delay: 0.1s;">
          <div class="stat-icon" style="background: linear-gradient(135deg, #3b82f6, #2563eb);">
            🚚
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ parcelStore.stats.inTransit }}</div>
            <div class="stat-label">In Transit</div>
          </div>
        </div>
        
        <div class="stat-card card fade-in" style="animation-delay: 0.2s;">
          <div class="stat-icon" style="background: linear-gradient(135deg, #10b981, #059669);">
            ✓
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ parcelStore.stats.delivered }}</div>
            <div class="stat-label">Delivered</div>
          </div>
        </div>
        
        <div class="stat-card card fade-in" style="animation-delay: 0.3s;">
          <div class="stat-icon" style="background: linear-gradient(135deg, #ef4444, #dc2626);">
            ⚠
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ parcelStore.stats.failed }}</div>
            <div class="stat-label">Failed</div>
          </div>
        </div>
      </div>
      
      <!-- Main Content Grid -->
      <div class="content-grid">
        <!-- Live Map Preview -->
        <div class="map-preview card">
          <h3>Live Tracking Map</h3>
          <div class="map-container">
            <LiveMap :parcels="parcelStore.activeParcels" :height="400" />
          </div>
          <router-link to="/map" class="btn btn-primary mt-2">
            View Full Map
          </router-link>
        </div>
        
        <!-- Recent Updates -->
        <div class="recent-updates card">
          <h3>Recent Location Updates</h3>
          <div class="updates-list">
            <div
              v-for="[parcelId, update] in recentUpdates"
              :key="parcelId"
              class="update-item"
            >
              <div class="update-icon pulse">📍</div>
              <div class="update-content">
                <div class="update-tracking">{{ update.trackingNumber }}</div>
                <div class="update-location text-muted">
                  {{ update.longitude.toFixed(4) }}, {{ update.latitude.toFixed(4) }}
                </div>
              </div>
              <div class="update-time text-muted">
                {{ formatTime(update.timestamp) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useParcelStore } from '../stores/parcel'
import Navbar from '../components/Navbar.vue'
import LiveMap from '../components/LiveMap.vue'

const parcelStore = useParcelStore()
const recentUpdates = computed(() => {
  return Array.from(parcelStore.locationUpdates.entries())
    .sort((a, b) => b[1].timestamp - a[1].timestamp)
    .slice(0, 10)
})

const formatTime = (timestamp) => {
  const now = Date.now()
  const diff = now - timestamp
  
  if (diff < 1000) return 'Just now'
  if (diff < 60000) return `${Math.floor(diff / 1000)}s ago`
  if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`
  return `${Math.floor(diff / 3600000)}h ago`
}

onMounted(async () => {
  await parcelStore.fetchParcels()
  await parcelStore.fetchStats()
  parcelStore.connectWebSocket()
  
  // Refresh stats every 10 seconds
  const interval = setInterval(() => {
    parcelStore.fetchStats()
  }, 10000)
  
  onUnmounted(() => {
    clearInterval(interval)
  })
})

onUnmounted(() => {
  parcelStore.disconnectWebSocket()
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: var(--bg-primary);
}

.dashboard-content {
  padding: var(--spacing-2xl);
  max-width: 1400px;
  margin: 0 auto;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-2xl);
}

.connection-status {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-danger);
  animation: pulse 2s infinite;
}

.status-dot.connected {
  background: var(--color-success);
}

.stats-grid {
  margin-bottom: var(--spacing-2xl);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: var(--font-size-3xl);
  font-weight: 800;
  line-height: 1;
  margin-bottom: var(--spacing-xs);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--spacing-lg);
}

.map-preview h3,
.recent-updates h3 {
  margin-bottom: var(--spacing-lg);
}

.map-container {
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: var(--spacing-md);
}

.updates-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  max-height: 500px;
  overflow-y: auto;
}

.update-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.update-item:hover {
  background: var(--bg-hover);
  transform: translateX(4px);
}

.update-icon {
  font-size: 24px;
}

.update-content {
  flex: 1;
}

.update-tracking {
  font-weight: 600;
  margin-bottom: var(--spacing-xs);
}

.update-location {
  font-size: var(--font-size-xs);
}

.update-time {
  font-size: var(--font-size-xs);
  white-space: nowrap;
}

@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
