<template>
  <nav class="navbar">
    <div class="navbar-content">
      <div class="navbar-brand">
        <h2 class="brand-text">RouteMaster Live</h2>
      </div>
      
      <div class="navbar-menu">
        <router-link to="/dashboard" class="nav-link" active-class="active">
          📊 Dashboard
        </router-link>
        <router-link to="/map" class="nav-link" active-class="active">
          🗺️ Map
        </router-link>
        <router-link to="/parcels" class="nav-link" active-class="active">
          📦 Parcels
        </router-link>
        <router-link to="/driver" class="nav-link" active-class="active">
          🚚 Driver View
        </router-link>
      </div>
      
      <div class="navbar-actions">
        <div class="user-info">
          <span class="user-name">{{ authStore.username }}</span>
          <span class="badge badge-primary">{{ userRole }}</span>
        </div>
        <button @click="handleLogout" class="btn btn-secondary">
          Logout
        </button>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const userRole = computed(() => {
  if (authStore.isAdmin) return 'Admin'
  if (authStore.isDriver) return 'Driver'
  return 'Viewer'
})

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--spacing-md) var(--spacing-2xl);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-2xl);
}

.navbar-brand {
  flex-shrink: 0;
}

.brand-text {
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: var(--font-size-xl);
  margin: 0;
}

.navbar-menu {
  display: flex;
  gap: var(--spacing-md);
  flex: 1;
}

.nav-link {
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  text-decoration: none;
  font-weight: 500;
  transition: all var(--transition-base);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.nav-link:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.nav-link.active {
  background: var(--color-primary);
  color: white;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.user-name {
  font-weight: 600;
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .navbar-content {
    flex-direction: column;
    gap: var(--spacing-md);
  }
  
  .navbar-menu {
    width: 100%;
    justify-content: center;
  }
  
  .navbar-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
