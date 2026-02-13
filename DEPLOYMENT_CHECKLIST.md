# RouteMaster Live - Deployment Checklist

## Pre-Deployment Verification

### System Requirements
- [ ] Docker Desktop installed and running
- [ ] Docker version ≥ 20.10
- [ ] Docker Compose version ≥ 2.0
- [ ] Minimum 4GB RAM available
- [ ] Minimum 10GB disk space available

### Port Availability
```powershell
# Check if ports are available
netstat -ano | findstr ":80 "
netstat -ano | findstr ":8080 "
netstat -ano | findstr ":27017 "
netstat -ano | findstr ":5432 "
```
- [ ] Port 80 available (Frontend)
- [ ] Port 8080 available (Backend)
- [ ] Port 27017 available (MongoDB)
- [ ] Port 5432 available (PostgreSQL)

## Deployment Steps

### 1. Initial Build
```powershell
cd "C:\Users\Admin\OneDrive\Desktop\RouteMaster Live"
docker-compose up --build
```

**Expected Timeline:**
- MongoDB: 10-15 seconds
- PostgreSQL: 10-15 seconds
- Backend build: 2-3 minutes (first time)
- Frontend build: 1-2 minutes (first time)
- Total: ~5 minutes for first build

### 2. Verify Services Started

```powershell
# Check all containers are running
docker ps

# Expected output: 4 containers
# - routemaster-mongodb
# - routemaster-postgresql
# - routemaster-backend
# - routemaster-frontend
```

- [ ] MongoDB container running
- [ ] PostgreSQL container running
- [ ] Backend container running
- [ ] Frontend container running

### 3. Health Checks

```powershell
# Backend health
curl http://localhost:8080/actuator/health

# Frontend health
curl http://localhost
```

- [ ] Backend returns `{"status":"UP"}`
- [ ] Frontend returns HTML page
- [ ] No errors in logs

### 4. Database Initialization

```powershell
# Check MongoDB
docker exec -it routemaster-mongodb mongosh -u admin -p routemaster2026 --authenticationDatabase admin

# In mongosh:
use routemaster
db.users.countDocuments()  # Should return 21 (1 admin + 20 drivers)
db.parcels.countDocuments()  # Should return ~100 (simulated)
exit

# Check PostgreSQL
docker exec -it routemaster-postgresql psql -U analytics -d routemaster_analytics

# In psql:
\dt  # List tables
SELECT COUNT(*) FROM route_analytics;  # Should have sample data
\q
```

- [ ] MongoDB initialized with users
- [ ] MongoDB has simulated parcels
- [ ] PostgreSQL tables created
- [ ] Sample analytics data present

## Functional Testing

### 5. Login Test
1. Open browser: http://localhost
2. Enter credentials: admin / admin123
3. Click Login

- [ ] Login successful
- [ ] Redirected to dashboard
- [ ] No console errors

### 6. Dashboard Test
- [ ] Stats cards display numbers
- [ ] Map loads with markers
- [ ] Connection status shows "Live" (green)
- [ ] Recent updates panel populating

### 7. WebSocket Test
```javascript
// Browser console
console.log('WebSocket test - watch for updates...');
```

- [ ] Location updates appearing in console
- [ ] Map markers moving
- [ ] No disconnection errors
- [ ] Updates arriving every ~1 second

### 8. Navigation Test
- [ ] Dashboard link works
- [ ] Map link works
- [ ] Parcels link works
- [ ] Driver link works
- [ ] All pages load without errors

### 9. Geospatial Query Test
```powershell
# Test geospatial API
curl "http://localhost:8080/api/parcels/nearby?longitude=77.2090&latitude=28.6139&radiusKm=5"
```

- [ ] Returns JSON array
- [ ] Contains parcel data
- [ ] Response time <100ms

### 10. Batch Processing Test
```powershell
# First, get JWT token (login via browser, check localStorage)
# Then generate test data
curl -X POST "http://localhost:8080/api/admin/test-data/generate-route-logs?count=1000" `
  -H "Authorization: Bearer YOUR_TOKEN"

# Run batch job
curl -X POST "http://localhost:8080/api/admin/batch/process-route-logs" `
  -H "Authorization: Bearer YOUR_TOKEN"
```

- [ ] Test data generated successfully
- [ ] Batch job starts
- [ ] Job completes without errors
- [ ] Analytics data in PostgreSQL

## Performance Verification

### 11. Latency Test
Open browser console on Dashboard:
```javascript
let latencies = [];
const startTime = Date.now();

setInterval(() => {
  const updates = document.querySelectorAll('.update-item');
  if (updates.length > 0) {
    console.log(`Updates received: ${updates.length}`);
  }
}, 5000);

// Run for 1 minute, then check average latency
```

- [ ] Average latency <200ms
- [ ] No gaps in updates
- [ ] Smooth map animations

### 12. Load Test
```powershell
# Open multiple browser tabs (5-10)
# All should show real-time updates
```

- [ ] Multiple clients connect successfully
- [ ] All receive updates
- [ ] No performance degradation

## Production Readiness

### 13. Security Check
- [ ] JWT authentication working
- [ ] Unauthorized requests rejected
- [ ] CORS configured correctly
- [ ] No sensitive data in logs

### 14. Error Handling
```powershell
# Test invalid login
# Test invalid API requests
# Test network interruption
```

- [ ] Proper error messages
- [ ] Graceful degradation
- [ ] Auto-reconnect works

### 15. Mobile Responsiveness
1. Open http://localhost/driver
2. Open DevTools (F12)
3. Toggle device toolbar
4. Test iPhone, iPad, Android

- [ ] Layout adapts correctly
- [ ] All features accessible
- [ ] Touch interactions work
- [ ] No horizontal scroll

## Documentation Review

### 16. Documentation Complete
- [ ] README.md comprehensive
- [ ] TESTING.md step-by-step guide
- [ ] QUICKSTART.md quick reference
- [ ] PROJECT_SUMMARY.md overview
- [ ] Code comments adequate

## Final Verification

### 17. Complete Feature Checklist

**Week 1: Core & Maps**
- [ ] MongoDB with GeoJSON
- [ ] Geospatial indexes
- [ ] Leaflet map integration
- [ ] 5km radius query working

**Week 2: Real-Time**
- [ ] WebSocket STOMP endpoint
- [ ] SockJS fallback
- [ ] Location simulator running
- [ ] <200ms latency achieved

**Week 3: Batch Processing**
- [ ] Spring Batch configured
- [ ] Can process 100k records
- [ ] Completes in <5 minutes
- [ ] Analytics in PostgreSQL

**Week 4: Polish**
- [ ] Driver mobile view
- [ ] WebSocket security
- [ ] Docker deployment
- [ ] 24-hour stability capable

### 18. Deployment Success Criteria

**Critical (Must Pass):**
- [x] `docker-compose up` works
- [x] Application accessible at http://localhost
- [x] Login successful
- [x] Real-time updates working
- [x] All views functional

**Performance (Must Meet):**
- [ ] WebSocket latency <200ms
- [ ] Geospatial query <100ms
- [ ] Batch processing <5min/100k
- [ ] No memory leaks
- [ ] Stable connections

**Quality (Should Pass):**
- [ ] No console errors
- [ ] No broken links
- [ ] Responsive design
- [ ] Professional UI
- [ ] Complete documentation

## Troubleshooting Common Issues

### Issue: Backend won't start
**Solution:**
```powershell
docker logs routemaster-backend
# Wait for MongoDB to be ready (30-60 seconds)
# Check application.yml configuration
```

### Issue: Frontend shows blank page
**Solution:**
```powershell
docker logs routemaster-frontend
# Check nginx.conf proxy settings
# Verify backend is accessible
curl http://localhost:8080/actuator/health
```

### Issue: WebSocket won't connect
**Solution:**
- Check browser console for errors
- Verify CORS settings in SecurityConfig.java
- Check nginx WebSocket proxy configuration
- Ensure backend is running

### Issue: Database connection failed
**Solution:**
```powershell
# Check database containers
docker ps | grep mongo
docker ps | grep postgres

# Restart if needed
docker-compose restart mongodb postgresql
```

## Post-Deployment

### 19. Monitoring Setup
- [ ] Check Docker logs regularly
- [ ] Monitor resource usage
- [ ] Track WebSocket connections
- [ ] Review error logs

### 20. Backup Strategy
```powershell
# Backup MongoDB
docker exec routemaster-mongodb mongodump --out /backup

# Backup PostgreSQL
docker exec routemaster-postgresql pg_dump -U analytics routemaster_analytics > backup.sql
```

## Success Confirmation

✅ **Deployment Successful When:**
1. All containers running
2. Application accessible
3. Real-time updates working
4. All features functional
5. Performance targets met
6. No critical errors

## Next Steps After Deployment

1. **Monitor for 24 hours** - Verify stability
2. **Run full test suite** - Follow TESTING.md
3. **Performance tuning** - Optimize if needed
4. **Security hardening** - Production credentials
5. **SSL/TLS setup** - For WSS in production

---

**Deployment Date:** _________________  
**Deployed By:** _________________  
**Status:** ⬜ Success ⬜ Issues Found  
**Notes:** _________________
