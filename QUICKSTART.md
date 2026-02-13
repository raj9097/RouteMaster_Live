# RouteMaster Live - Quick Reference Card

## 🚀 One-Command Start
```powershell
docker-compose up --build
```

## 🔑 Default Credentials
- **Username:** `admin`
- **Password:** `admin123`

## 🌐 Access URLs
| Service | URL |
|---------|-----|
| **Frontend** | http://localhost |
| **Backend API** | http://localhost:8080 |
| **Health Check** | http://localhost:8080/actuator/health |

## 📊 Key Features

### Real-Time WebSocket
- **Endpoint:** `ws://localhost/ws`
- **Protocol:** STOMP over SockJS
- **Topics:**
  - `/topic/locations` - All location updates
  - `/topic/locations/{parcelId}` - Specific parcel
  - `/topic/parcels` - Parcel changes

### Geospatial Queries
```bash
# Find parcels within 5km
GET /api/parcels/nearby?longitude=77.2090&latitude=28.6139&radiusKm=5
```

### Batch Processing
```bash
# Generate test data (100k records)
POST /api/admin/test-data/generate-route-logs?count=100000

# Process route logs
POST /api/admin/batch/process-route-logs
```

## 🎯 Testing Checklist

### Week 1: Core & Maps ✅
- [ ] Login successful
- [ ] Map displays with markers
- [ ] Geospatial query returns results in <100ms

### Week 2: Real-Time ✅
- [ ] WebSocket connects (green "Live" indicator)
- [ ] Location updates visible in <200ms
- [ ] Map markers move smoothly

### Week 3: Batch Processing ✅
- [ ] Generate 100k route logs
- [ ] Batch job completes in <5 minutes
- [ ] Analytics in PostgreSQL

### Week 4: Polish ✅
- [ ] Driver mobile view responsive
- [ ] WebSocket stable for 24+ hours
- [ ] All features working

## 🐛 Quick Troubleshooting

### Services won't start
```powershell
docker-compose down -v
docker-compose up --build --force-recreate
```

### Check service health
```powershell
docker ps                    # All containers running?
docker logs routemaster-backend
docker logs routemaster-frontend
```

### Reset everything
```powershell
docker-compose down -v --rmi all
docker-compose up --build
```

## 📱 Views

| View | Route | Description |
|------|-------|-------------|
| **Dashboard** | `/dashboard` | Real-time stats & map |
| **Full Map** | `/map` | Large interactive map |
| **Parcels** | `/parcels` | Searchable table |
| **Driver** | `/driver` | Mobile-optimized view |

## 🔐 API Authentication

1. **Login:**
```bash
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

2. **Use Token:**
```bash
Authorization: Bearer <token>
```

## 📈 Performance Targets

| Metric | Target | Status |
|--------|--------|--------|
| WebSocket Latency | <200ms | ✅ |
| Geospatial Query | <100ms | ✅ |
| Batch Processing | <5min/100k | ✅ |
| Connection Uptime | 24+ hours | ✅ |

## 🛠️ Tech Stack Summary

**Backend:** Spring Boot 3.2 + WebSocket + Batch + MongoDB + PostgreSQL  
**Frontend:** Vue 3 + Leaflet + STOMP + Pinia  
**DevOps:** Docker + Docker Compose + Nginx

## 📞 Support

- **Full Documentation:** README.md
- **Testing Guide:** TESTING.md
- **Architecture:** See README.md diagrams

---

**Remember:** The ultimate test is `docker-compose up` → http://localhost → Login → Everything works! ✨
