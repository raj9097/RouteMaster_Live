import { defineStore } from 'pinia'
import axios from 'axios'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('token') || null,
        username: localStorage.getItem('username') || null,
        roles: JSON.parse(localStorage.getItem('roles') || '[]')
    }),

    getters: {
        isAuthenticated: (state) => !!state.token,
        isAdmin: (state) => state.roles.includes('ROLE_ADMIN'),
        isDriver: (state) => state.roles.includes('ROLE_DRIVER')
    },

    actions: {
        async login(username, password) {
            try {
                const response = await axios.post('/api/auth/login', { username, password })
                const { token, username: user, roles } = response.data

                this.token = token
                this.username = user
                this.roles = roles

                localStorage.setItem('token', token)
                localStorage.setItem('username', user)
                localStorage.setItem('roles', JSON.stringify(roles))

                // Set default authorization header
                axios.defaults.headers.common['Authorization'] = `Bearer ${token}`

                return true
            } catch (error) {
                console.error('Login failed:', error)
                return false
            }
        },

        async register(username, password, email, fullName) {
            try {
                const response = await axios.post('/api/auth/register', {
                    username,
                    password,
                    email,
                    fullName
                })

                const { token, username: user, roles } = response.data

                this.token = token
                this.username = user
                this.roles = roles

                localStorage.setItem('token', token)
                localStorage.setItem('username', user)
                localStorage.setItem('roles', JSON.stringify(roles))

                axios.defaults.headers.common['Authorization'] = `Bearer ${token}`

                return true
            } catch (error) {
                console.error('Registration failed:', error)
                return false
            }
        },

        logout() {
            this.token = null
            this.username = null
            this.roles = []

            localStorage.removeItem('token')
            localStorage.removeItem('username')
            localStorage.removeItem('roles')

            delete axios.defaults.headers.common['Authorization']
        },

        initializeAuth() {
            if (this.token) {
                axios.defaults.headers.common['Authorization'] = `Bearer ${this.token}`
            }
        }
    }
})
