# RouteMaster Live - Testing Guide

## Prerequisites Check
Before starting, ensure:
- [ ] Docker is installed and running
- [ ] Docker Compose is installed
- [ ] Ports 80, 8080, 27017, 5432 are available
- [ ] At least 4GB RAM available for Docker

## Step-by-Step Testing

### 1. Initial Setup (5 minutes)

```powershell
# Navigate to project directory
cd "C:\Users\Admin\OneDrive\Desktop\RouteMaster Live"

# Start all services
docker-compose up --build
```

**Expected Output:**
- MongoDB starts and initializes
- PostgreSQL starts and initializes
- Backend builds and starts (may take 2-3 minutes)
- Frontend builds and starts
- All health checks pass

**Verification:**
```powershell
# Check all containers are running
docker ps

# Should see 4 containers:
# - routemaster-mongodb
# - routemaster-postgresql
# - routemaster-backend
# - routemaster-frontend
```

### 2. Week 1 Test: Geospatial Query (2 minutes)

**Objective:** Query parcels within 5km radius

1. **Access Application:**
   - Open browser: http://localhost
   - Login: admin / admin123

2. **Verify Map Loads:**
   - Dashboard should show map with parcel markers
   - Check that 100 simulated parcels are visible

3. **Test Geospatial API:**
```powershell
# Get JWT token first (from browser console after login)
# Or use this test:

# Find parcels near New Delhi city center (5km radius)
curl "http://localhost:8080/api/parcels/nearby?longitude=77.2090&latitude=28.6139&radiusKm=5"
```

**Expected Result:**
- Returns JSON array of parcels within 5km
- Query completes in <100ms
- Each parcel has `currentLocation` with GeoJSON coordinates

**✅ Pass Criteria:** Geospatial query returns results in <100ms

### 3. Week 2 Test: WebSocket Latency (10 minutes)

**Objective:** Verify <200ms latency for location updates

1. **Open Dashboard:**
   - Navigate to http://localhost/dashboard
   - Open Browser DevTools (F12)
   - Go to Console tab

2. **Monitor WebSocket:**
   - Go to Network tab → WS filter
   - Click on `/ws` connection
   - Watch Messages tab

3. **Measure Latency:**
```javascript
// Paste in browser console:
let latencies = [];
const originalLog = console.log;

// Intercept location updates
window.addEventListener('message', (event) => {
  const now = Date.now();
  const data = JSON.parse(event.data);
  if (data.timestamp) {
    const latency = now - data.timestamp;
    latencies.push(latency);
    console.log(`Latency: ${latency}ms`);
  }
});

// After 1 minute, check average
setTimeout(() => {
  const avg = latencies.reduce((a,b) => a+b, 0) / latencies.length;
  console.log(`Average latency: ${avg.toFixed(2)}ms`);
  console.log(`Max latency: ${Math.max(...latencies)}ms`);
}, 60000);
```

4. **Visual Verification:**
   - Watch map markers move in real-time
   - Check "Recent Location Updates" panel
   - Verify connection status shows "Live" (green dot)

**Expected Result:**
- Average latency <200ms
- Map markers update smoothly
- No connection dropouts

**✅ Pass Criteria:** Average latency <200ms, visual updates smooth

### 4. Week 3 Test: Batch Processing (15 minutes)

**Objective:** Process 100,000 records in <5 minutes

1. **Generate Test Data:**
```powershell
# Get JWT token (login first, copy from browser localStorage)
$token = "YOUR_JWT_TOKEN_HERE"

# Generate 100,000 route logs
curl -X POST "http://localhost:8080/api/admin/test-data/generate-route-logs?count=100000" `
  -H "Authorization: Bearer $token"
```

**Wait for generation to complete** (may take 2-3 minutes)

2. **Run Batch Job:**
```powershell
# Trigger batch processing
curl -X POST "http://localhost:8080/api/admin/batch/process-route-logs" `
  -H "Authorization: Bearer $token"
```

3. **Monitor Progress:**
```powershell
# Watch backend logs
docker logs -f routemaster-backend

# Look for:
# - "Batch job started"
# - Processing progress
# - "Batch job completed"
```

4. **Verify Results:**
```powershell
# Check PostgreSQL for results
docker exec -it routemaster-postgresql psql -U analytics -d routemaster_analytics

# Run query:
SELECT COUNT(*) FROM route_analytics;
SELECT AVG(average_speed), AVG(total_distance) FROM route_analytics;

# Exit: \q
```

**Expected Result:**
- 100,000 records processed
- Processing time <5 minutes
- Analytics data in PostgreSQL

**✅ Pass Criteria:** 100k records processed in <5 minutes

### 5. Week 4 Test: 24-Hour Stability (24 hours)

**Objective:** Maintain WebSocket connection for 24+ hours

1. **Setup Long-Running Test:**
   - Open Dashboard in browser
   - Keep tab active (prevent sleep)
   - Note start time

2. **Monitor Connection:**
```javascript
// Browser console - connection monitor
let startTime = Date.now();
let disconnects = 0;

setInterval(() => {
  const uptime = ((Date.now() - startTime) / 1000 / 60 / 60).toFixed(2);
  console.log(`Uptime: ${uptime} hours, Disconnects: ${disconnects}`);
}, 60000); // Log every minute
```

3. **Check Periodically:**
   - Every 4 hours, verify green "Live" status
   - Check that location updates continue
   - Monitor Docker container health

**Expected Result:**
- Connection stays active for 24+ hours
- No manual reconnection needed
- Continuous location updates

**✅ Pass Criteria:** 24+ hours without connection loss

### 6. Additional Tests

#### Mobile Responsiveness
1. Open http://localhost/driver
2. Open DevTools → Toggle device toolbar
3. Test on iPhone, iPad, Android viewports
4. Verify layout adapts correctly

#### Security Test
```powershell
# Test without authentication (should fail)
curl http://localhost:8080/api/parcels

# Test with invalid token (should fail)
curl -H "Authorization: Bearer invalid_token" http://localhost:8080/api/parcels
```

## Performance Benchmarks

| Test | Target | Typical Result |
|------|--------|----------------|
| Geospatial Query | <100ms | 20-50ms |
| WebSocket Latency | <200ms | 50-150ms |
| Batch Processing | <5min for 100k | 2-4min |
| Connection Uptime | 24+ hours | Unlimited |
| Page Load | <3s | 1-2s |

## Troubleshooting

### Backend won't start
```powershell
# Check logs
docker logs routemaster-backend

# Common issues:
# - MongoDB not ready: Wait 30s and retry
# - Port 8080 in use: Stop conflicting service
```

### Frontend shows blank page
```powershell
# Check nginx logs
docker logs routemaster-frontend

# Verify backend is accessible
curl http://localhost:8080/actuator/health
```

### WebSocket won't connect
```powershell
# Check browser console for errors
# Verify CORS settings in backend
# Check nginx proxy configuration
```

### Batch job fails
```powershell
# Ensure test data exists
curl -H "Authorization: Bearer $token" http://localhost:8080/api/admin/test-data/generate-route-logs?count=1000

# Check PostgreSQL is running
docker ps | grep postgresql
```

## Success Checklist

- [ ] All 4 Docker containers running
- [ ] Can login to application
- [ ] Map displays with markers
- [ ] Geospatial query works (<100ms)
- [ ] WebSocket connects (green "Live" indicator)
- [ ] Location updates in real-time (<200ms)
- [ ] Batch job processes 100k records (<5min)
- [ ] Mobile driver view responsive
- [ ] 24-hour connection stability

## Clean Up

```powershell
# Stop all services
docker-compose down

# Remove volumes (clean database)
docker-compose down -v

# Remove images
docker-compose down --rmi all
```

## Next Steps

After successful testing:
1. Review architecture in README.md
2. Explore code structure
3. Customize for production use
4. Add SSL/TLS for WSS
5. Configure production database credentials
6. Set up monitoring and logging

---

**Need Help?** Check README.md troubleshooting section or review Docker logs.
