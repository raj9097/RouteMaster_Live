import { defineStore } from 'pinia'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import axios from 'axios'

export const useParcelStore = defineStore('parcel', {
    state: () => ({
        parcels: [],
        selectedParcel: null,
        stompClient: null,
        connected: false,
        locationUpdates: new Map(),
        stats: {
            pending: 0,
            inTransit: 0,
            delivered: 0,
            failed: 0
        }
    }),

    getters: {
        activeParcels: (state) => state.parcels.filter(p =>
            p.status === 'IN_TRANSIT' || p.status === 'OUT_FOR_DELIVERY'
        ),

        parcelCount: (state) => state.parcels.length,

        getParcelById: (state) => (id) => state.parcels.find(p => p.id === id)
    },

    actions: {
        async fetchParcels() {
            try {
                const response = await axios.get('/api/parcels')
                this.parcels = response.data
            } catch (error) {
                console.error('Failed to fetch parcels:', error)
            }
        },

        async fetchStats() {
            try {
                const response = await axios.get('/api/parcels/stats/count')
                this.stats = response.data
            } catch (error) {
                console.error('Failed to fetch stats:', error)
            }
        },

        async fetchParcelsByStatus(status) {
            try {
                const response = await axios.get(`/api/parcels/status/${status}`)
                return response.data
            } catch (error) {
                console.error('Failed to fetch parcels by status:', error)
                return []
            }
        },

        async findNearbyParcels(longitude, latitude, radiusKm = 5) {
            try {
                const response = await axios.get('/api/parcels/nearby', {
                    params: { longitude, latitude, radiusKm }
                })
                return response.data
            } catch (error) {
                console.error('Failed to find nearby parcels:', error)
                return []
            }
        },

        connectWebSocket() {
            const socket = new SockJS('/ws')

            this.stompClient = new Client({
                webSocketFactory: () => socket,
                debug: (str) => console.log('STOMP:', str),
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,

                onConnect: () => {
                    console.log('WebSocket connected')
                    this.connected = true

                    // Subscribe to all location updates
                    this.stompClient.subscribe('/topic/locations', (message) => {
                        const update = JSON.parse(message.body)
                        this.handleLocationUpdate(update)
                    })

                    // Subscribe to parcel updates
                    this.stompClient.subscribe('/topic/parcels', (message) => {
                        const parcel = JSON.parse(message.body)
                        this.handleParcelUpdate(parcel)
                    })
                },

                onDisconnect: () => {
                    console.log('WebSocket disconnected')
                    this.connected = false
                },

                onStompError: (frame) => {
                    console.error('STOMP error:', frame)
                }
            })

            this.stompClient.activate()
        },

        disconnectWebSocket() {
            if (this.stompClient) {
                this.stompClient.deactivate()
                this.connected = false
            }
        },

        handleLocationUpdate(update) {
            // Store the latest location update
            this.locationUpdates.set(update.parcelId, update)

            // Update parcel in the list
            const parcel = this.parcels.find(p => p.id === update.parcelId)
            if (parcel && parcel.currentLocation) {
                parcel.currentLocation.coordinates = [update.longitude, update.latitude]
            }
        },

        handleParcelUpdate(parcel) {
            const index = this.parcels.findIndex(p => p.id === parcel.id)
            if (index !== -1) {
                this.parcels[index] = parcel
            } else {
                this.parcels.push(parcel)
            }
        },

        subscribeToParcel(parcelId) {
            if (this.stompClient && this.connected) {
                return this.stompClient.subscribe(`/topic/locations/${parcelId}`, (message) => {
                    const update = JSON.parse(message.body)
                    this.handleLocationUpdate(update)
                })
            }
        }
    }
})
