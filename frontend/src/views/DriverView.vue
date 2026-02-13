<template>
  <div class="driver-view">
    <div class="driver-header">
      <h2>🚚 Driver Dashboard</h2>
      <button @click="handleLogout" class="btn btn-secondary">Logout</button>
    </div>
    
    <div class="driver-content">
      <!-- Driver Stats -->
      <div class="driver-stats">
        <div class="stat-card-mobile card">
          <div class="stat-icon">📦</div>
          <div class="stat-info">
            <div class="stat-value">{{ myParcels.length }}</div>
            <div class="stat-label">My Deliveries</div>
          </div>
        </div>
        
        <div class="stat-card-mobile card">
          <div class="stat-icon">✓</div>
          <div class="stat-info">
            <div class="stat-value">{{ completedCount }}</div>
            <div class="stat-label">Completed</div>
          </div>
        </div>
        
        <div class="stat-card-mobile card">
          <div class="stat-icon">🚚</div>
          <div class="stat-info">
            <div class="stat-value">{{ inTransitCount }}</div>
            <div class="stat-label">In Transit</div>
          </div>
        </div>
      </div>
      
      <!-- My Parcels List -->
      <div class="my-parcels card">
        <h3>Today's Deliveries</h3>
        <div class="parcel-list">
          <div
            v-for="parcel in myParcels"
            :key="parcel.id"
            class="parcel-card"
            :class="{ completed: parcel.status === 'DELIVERED' }"
          >
            <div class="parcel-header-mobile">
              <div class="tracking-number">{{ parcel.trackingNumber }}</div>
              <span class="badge" :class="getStatusClass(parcel.status)">
                {{ parcel.status }}
              </span>
            </div>
            
            <div class="parcel-details">
              <div class="detail-row">
                <span class="detail-label">Recipient:</span>
                <span class="detail-value">{{ parcel.recipientName || 'N/A' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Address:</span>
                <span class="detail-value">{{ parcel.recipientAddress || 'N/A' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Priority:</span>
                <span class="badge" :class="getPriorityClass(parcel.priority)">
                  {{ parcel.priority || 'MEDIUM' }}
                </span>
              </div>
            </div>
            
            <div class="parcel-actions">
              <button
                v-if="parcel.status !== 'DELIVERED'"
                class="btn btn-success w-full"
                @click="markAsDelivered(parcel.id)"
              >
                Mark as Delivered
              </button>
            </div>
          </div>
        </div>
        
        <div v-if="myParcels.length === 0" class="empty-state">
          <p>No deliveries assigned</p>
        </div>
      </div>
      
      <!-- Mini Map -->
      <div class="driver-map card">
        <h3>My Route</h3>
        <LiveMap :parcels="myParcels" :height="300" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useParcelStore } from '../stores/parcel'
import LiveMap from '../components/LiveMap.vue'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()
const parcelStore = useParcelStore()

// Simulated driver ID (in production, this would come from auth)
const driverId = ref('driver-1')

const myParcels = computed(() => {
  return parcelStore.parcels.filter(p => p.assignedDriverId === driverId.value)
})

const completedCount = computed(() => {
  return myParcels.value.filter(p => p.status === 'DELIVERED').length
})

const inTransitCount = computed(() => {
  return myParcels.value.filter(p => 
    p.status === 'IN_TRANSIT' || p.status === 'OUT_FOR_DELIVERY'
  ).length
})

const getStatusClass = (status) => {
  const classes = {
    'PENDING': 'badge-info',
    'IN_TRANSIT': 'badge-primary',
    'OUT_FOR_DELIVERY': 'badge-warning',
    'DELIVERED': 'badge-success',
    'FAILED': 'badge-danger'
  }
  return classes[status] || 'badge-info'
}

const getPriorityClass = (priority) => {
  const classes = {
    'LOW': 'badge-info',
    'MEDIUM': 'badge-primary',
    'HIGH': 'badge-warning',
    'URGENT': 'badge-danger'
  }
  return classes[priority] || 'badge-primary'
}

const markAsDelivered = async (parcelId) => {
  try {
    const parcel = parcelStore.parcels.find(p => p.id === parcelId)
    if (parcel) {
      parcel.status = 'DELIVERED'
      await axios.put(`/api/parcels/${parcelId}`, parcel)
    }
  } catch (error) {
    console.error('Failed to update parcel:', error)
  }
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(async () => {
  await parcelStore.fetchParcels()
  if (!parcelStore.connected) {
    parcelStore.connectWebSocket()
  }
})
</script>

<style scoped>
.driver-view {
  min-height: 100vh;
  background: var(--bg-primary);
  padding: var(--spacing-lg);
}

.driver-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-2xl);
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
}

.driver-content {
  max-width: 600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.driver-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-md);
}

.stat-card-mobile {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
}

.stat-icon {
  font-size: 32px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: var(--font-size-2xl);
  font-weight: 800;
  line-height: 1;
  margin-bottom: var(--spacing-xs);
}

.stat-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  text-transform: uppercase;
}

.my-parcels h3,
.driver-map h3 {
  margin-bottom: var(--spacing-lg);
}

.parcel-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.parcel-card {
  padding: var(--spacing-lg);
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  border-left: 4px solid var(--color-primary);
  transition: all var(--transition-base);
}

.parcel-card.completed {
  border-left-color: var(--color-success);
  opacity: 0.7;
}

.parcel-card:hover {
  transform: translateX(4px);
}

.parcel-header-mobile {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.tracking-number {
  font-weight: 700;
  font-size: var(--font-size-lg);
  font-family: 'Courier New', monospace;
}

.parcel-details {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--font-size-sm);
}

.detail-label {
  color: var(--text-muted);
  font-weight: 600;
}

.detail-value {
  color: var(--text-primary);
}

.empty-state {
  padding: var(--spacing-2xl);
  text-align: center;
  color: var(--text-muted);
}

@media (max-width: 480px) {
  .driver-stats {
    grid-template-columns: 1fr;
  }
}
</style>
