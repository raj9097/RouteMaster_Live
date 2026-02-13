<template>
  <div class="parcels-view">
    <Navbar />
    
    <div class="parcels-content">
      <div class="parcels-header">
        <h1>Parcel Management</h1>
        <div class="search-box">
          <input
            v-model="searchQuery"
            type="text"
            class="input"
            placeholder="Search by tracking number..."
          />
        </div>
      </div>
      
      <div class="parcels-table-container card">
        <table class="parcels-table">
          <thead>
            <tr>
              <th>Tracking Number</th>
              <th>Status</th>
              <th>Driver</th>
              <th>Location</th>
              <th>Priority</th>
              <th>Last Updated</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="parcel in filteredParcels"
              :key="parcel.id"
              class="parcel-row"
            >
              <td class="tracking-cell">
                <strong>{{ parcel.trackingNumber }}</strong>
              </td>
              <td>
                <span class="badge" :class="getStatusClass(parcel.status)">
                  {{ parcel.status }}
                </span>
              </td>
              <td>{{ parcel.assignedDriverId || 'Unassigned' }}</td>
              <td class="location-cell">
                <span v-if="parcel.currentLocation">
                  {{ parcel.currentLocation.coordinates[1].toFixed(4) }},
                  {{ parcel.currentLocation.coordinates[0].toFixed(4) }}
                </span>
                <span v-else class="text-muted">N/A</span>
              </td>
              <td>
                <span class="badge" :class="getPriorityClass(parcel.priority)">
                  {{ parcel.priority || 'MEDIUM' }}
                </span>
              </td>
              <td class="text-muted">
                {{ formatDate(parcel.lastUpdated) }}
              </td>
            </tr>
          </tbody>
        </table>
        
        <div v-if="filteredParcels.length === 0" class="empty-state">
          <p>No parcels found</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useParcelStore } from '../stores/parcel'
import Navbar from '../components/Navbar.vue'

const parcelStore = useParcelStore()
const searchQuery = ref('')

const filteredParcels = computed(() => {
  if (!searchQuery.value) {
    return parcelStore.parcels
  }
  
  const query = searchQuery.value.toLowerCase()
  return parcelStore.parcels.filter(p =>
    p.trackingNumber.toLowerCase().includes(query)
  )
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

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleString()
}

onMounted(async () => {
  await parcelStore.fetchParcels()
  if (!parcelStore.connected) {
    parcelStore.connectWebSocket()
  }
})
</script>

<style scoped>
.parcels-view {
  min-height: 100vh;
  background: var(--bg-primary);
}

.parcels-content {
  padding: var(--spacing-2xl);
  max-width: 1400px;
  margin: 0 auto;
}

.parcels-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-2xl);
  gap: var(--spacing-lg);
}

.search-box {
  flex: 1;
  max-width: 400px;
}

.parcels-table-container {
  overflow-x: auto;
}

.parcels-table {
  width: 100%;
  border-collapse: collapse;
}

.parcels-table thead {
  background: var(--bg-tertiary);
}

.parcels-table th {
  padding: var(--spacing-md) var(--spacing-lg);
  text-align: left;
  font-weight: 600;
  font-size: var(--font-size-sm);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
  border-bottom: 2px solid var(--border-color);
}

.parcels-table td {
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--border-color);
}

.parcel-row {
  transition: all var(--transition-base);
}

.parcel-row:hover {
  background: var(--bg-tertiary);
}

.tracking-cell {
  font-family: 'Courier New', monospace;
}

.location-cell {
  font-size: var(--font-size-sm);
  font-family: 'Courier New', monospace;
}

.empty-state {
  padding: var(--spacing-2xl);
  text-align: center;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .parcels-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    max-width: 100%;
  }
}
</style>
