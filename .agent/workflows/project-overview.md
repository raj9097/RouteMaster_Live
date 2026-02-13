---
description: RouteMaster Live - Project Overview and Architecture
---

# RouteMaster Live - Real-Time Supply Chain Command Center

## Project Vision
A production-grade logistics platform tracking 10,000+ active parcels in real-time using WebSocket push technology, geospatial queries, and batch processing for analytics.

## Technology Stack

### Backend
- **Spring Boot 3.x** - Core application framework
- **Spring WebSocket (STOMP)** - Real-time bi-directional communication
- **Spring Batch** - Large-scale batch processing
- **Spring Security** - Authentication & WebSocket security
- **MongoDB** - Primary database with GeoJSON support
- **PostgreSQL** - Analytics database for batch processing results
- **Maven** - Build tool

### Frontend
- **Vue.js 3** - Reactive UI framework
- **Leaflet.js** - OpenStreetMap integration
- **SockJS + STOMP.js** - WebSocket client
- **Vite** - Build tool
- **Pinia** - State management

### DevOps
- **Docker & Docker Compose** - Containerization
- **Nginx** - Reverse proxy
- **WSS (WebSocket Secure)** - Secure WebSocket connections

## Core Features

### 1. WebSocket Push (STOMP Protocol)
- Persistent bi-directional connections
- Channel-based subscriptions per parcel
- Sub-200ms latency requirement
- SockJS fallback for compatibility
- Authentication integration

### 2. Geospatial Search
- MongoDB GeoJSON support (2dsphere index)
- Queries: "Find parcels within 5km radius"
- "Nearest delivery hub" calculations
- Real-time location updates

### 3. Batch Processing
- Spring Batch jobs for end-of-day operations
- Process 100,000+ records in <5 minutes
- Vehicle efficiency calculations
- Reconciliation reports
- Write to separate analytics DB

### 4. Real-Time Visualization
- Live map updates via Leaflet
- Parcel tracking dashboard
- Driver mobile view (responsive)
- 24-hour connection stability

## Week-by-Week Implementation Plan

### Week 1: Core Logistics & Maps
- [ ] MongoDB schema with GeoJSON
- [ ] Spring Boot backend structure
- [ ] Vue.js frontend with Vite
- [ ] Leaflet integration
- [ ] Basic CRUD operations
- **Test**: Query parcels within 5km radius

### Week 2: Real-Time Layer
- [ ] Spring WebSocket configuration
- [ ] STOMP endpoint setup
- [ ] SockJS fallback
- [ ] Location simulator service
- [ ] Vue WebSocket client
- [ ] Real-time map marker updates
- **Test**: <200ms latency from generation to display

### Week 3: Batch Processing
- [ ] Spring Batch configuration
- [ ] PostgreSQL analytics DB
- [ ] Route log processor
- [ ] Performance metrics calculation
- [ ] Scheduled job execution
- **Test**: 100,000 records in <5 minutes

### Week 4: Final Polish
- [ ] Driver mobile view (responsive)
- [ ] WebSocket security (WSS)
- [ ] Authentication & authorization
- [ ] Docker containerization
- [ ] docker-compose.yml
- [ ] Production optimizations
- **Test**: 24-hour stable WebSocket connection

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     Vue.js Frontend                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Dashboard   │  │  Map View    │  │ Driver View  │      │
│  │  (Admin)     │  │  (Leaflet)   │  │  (Mobile)    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                  │                  │              │
│         └──────────────────┴──────────────────┘              │
│                            │                                 │
│                    STOMP over WebSocket                      │
└────────────────────────────┼────────────────────────────────┘
                             │
┌────────────────────────────┼────────────────────────────────┐
│                   Spring Boot Backend                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         WebSocket STOMP Broker                       │   │
│  │  /topic/parcels/{id}  /topic/locations              │   │
│  └──────────────────────────────────────────────────────┘   │
│                            │                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   REST API   │  │  Simulator   │  │ Spring Batch │      │
│  │  Controller  │  │   Service    │  │    Jobs      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                  │                  │              │
└─────────┼──────────────────┼──────────────────┼─────────────┘
          │                  │                  │
    ┌─────┴─────┐      ┌────┴────┐      ┌─────┴─────┐
    │  MongoDB  │      │ MongoDB │      │PostgreSQL │
    │ (Primary) │      │(GeoJSON)│      │(Analytics)│
    └───────────┘      └─────────┘      └───────────┘
```

## Data Models

### Parcel (MongoDB)
```json
{
  "_id": "ObjectId",
  "trackingNumber": "RM-2026-001234",
  "status": "IN_TRANSIT",
  "currentLocation": {
    "type": "Point",
    "coordinates": [longitude, latitude]
  },
  "origin": { "type": "Point", "coordinates": [...] },
  "destination": { "type": "Point", "coordinates": [...] },
  "assignedDriver": "driver-id",
  "estimatedDelivery": "ISO-8601",
  "lastUpdated": "ISO-8601"
}
```

### Route Log (MongoDB → PostgreSQL via Batch)
```json
{
  "routeId": "route-001",
  "driverId": "driver-123",
  "vehicleId": "vehicle-456",
  "date": "2026-02-13",
  "locations": [
    { "timestamp": "...", "coordinates": [...], "speed": 45 }
  ]
}
```

## Performance Requirements
- **WebSocket Latency**: <200ms
- **Batch Processing**: 100,000 records in <5 minutes
- **Connection Stability**: 24+ hours continuous
- **Concurrent Users**: Support 1,000+ simultaneous connections
- **Geospatial Query**: <100ms for 5km radius search

## Security
- JWT-based authentication
- WebSocket handshake authentication
- WSS (TLS/SSL) for production
- Role-based access control (Admin, Driver, Viewer)

## Deployment
- Docker containers for all services
- docker-compose.yml for orchestration
- Environment-based configuration
- Health checks and auto-restart
- Volume persistence for databases

## Success Criteria
✅ `docker-compose up` starts entire stack
✅ Real-time map updates <200ms
✅ Geospatial queries functional
✅ Batch job processes 100k records <5min
✅ 24-hour connection stability
✅ Mobile-responsive driver view
✅ Secure WSS connections
