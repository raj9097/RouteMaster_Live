package com.routemaster.service;

import com.routemaster.model.RouteLog;
import com.routemaster.repository.RouteLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestDataGeneratorService {

    private final RouteLogRepository routeLogRepository;
    private final Random random = new Random();

    // City center coordinates (New Delhi, India)
    private static final double CENTER_LAT = 28.6139;
    private static final double CENTER_LON = 77.2090;
    private static final double RADIUS = 0.5; // degrees (~55km)

    /**
     * Generate test route logs for batch processing
     * This creates 100,000+ route logs for yesterday's date
     */
    public void generateTestRouteLogs(int count) {
        log.info("Generating {} test route logs...", count);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<RouteLog> routeLogs = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            RouteLog routeLog = new RouteLog();
            routeLog.setRouteId("route-" + String.format("%06d", i + 1));
            routeLog.setDriverId("driver-" + (i % 20 + 1));
            routeLog.setVehicleId("vehicle-" + (i % 50 + 1));
            routeLog.setDate(yesterday);

            // Generate location points for the route
            List<RouteLog.LocationPoint> locations = generateLocationPoints(50 + random.nextInt(50));
            routeLog.setLocations(locations);

            routeLogs.add(routeLog);

            // Save in batches of 1000
            if (routeLogs.size() >= 1000) {
                routeLogRepository.saveAll(routeLogs);
                routeLogs.clear();
                log.info("Saved {} route logs...", i + 1);
            }
        }

        // Save remaining
        if (!routeLogs.isEmpty()) {
            routeLogRepository.saveAll(routeLogs);
        }

        log.info("Successfully generated {} test route logs", count);
    }

    private List<RouteLog.LocationPoint> generateLocationPoints(int count) {
        List<RouteLog.LocationPoint> points = new ArrayList<>();

        // Start location
        double currentLat = CENTER_LAT + (random.nextDouble() - 0.5) * RADIUS;
        double currentLon = CENTER_LON + (random.nextDouble() - 0.5) * RADIUS;

        LocalDateTime timestamp = LocalDateTime.now().minusDays(1).withHour(8).withMinute(0);

        for (int i = 0; i < count; i++) {
            RouteLog.LocationPoint point = new RouteLog.LocationPoint();
            point.setTimestamp(timestamp.plusMinutes(i * 2));
            point.setLatitude(currentLat);
            point.setLongitude(currentLon);
            point.setSpeed(30.0 + random.nextDouble() * 40.0); // 30-70 km/h
            point.setHeading(random.nextDouble() * 360.0);

            points.add(point);

            // Move to next location
            currentLat += (random.nextDouble() - 0.5) * 0.01;
            currentLon += (random.nextDouble() - 0.5) * 0.01;
        }

        return points;
    }

    /**
     * Clear all route logs
     */
    public void clearRouteLogs() {
        routeLogRepository.deleteAll();
        log.info("Cleared all route logs");
    }
}
