package com.routemaster.service;

import com.routemaster.model.Parcel;
import com.routemaster.repository.ParcelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationSimulatorService {

    private final ParcelRepository parcelRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.simulator.enabled:true}")
    private boolean simulatorEnabled;

    @Value("${app.simulator.parcel-count:20}")
    private int parcelCount;

    private final Random random = new Random();
    // Changed key from Long to String
    private final Map<String, SimulatedRoute> activeRoutes = new ConcurrentHashMap<>();

    // City center coordinates (example: New Delhi, India)
    private static final double CENTER_LAT = 28.6139;
    private static final double CENTER_LON = 77.2090;
    private static final double RADIUS = 0.5; // degrees (~55km)

    @PostConstruct
    public void initialize() {
        if (simulatorEnabled) {
            log.info("Location simulator is ENABLED. Will simulate {} parcels.", parcelCount);
            initializeSimulatedParcels();
        } else {
            log.info("Location simulator is DISABLED.");
        }
    }

    private void initializeSimulatedParcels() {
        // Get or create parcels for simulation
        List<Parcel> existingParcels = parcelRepository.findByStatus(Parcel.ParcelStatus.IN_TRANSIT);

        if (existingParcels.size() < parcelCount) {
            log.info("Creating {} simulated parcels...", parcelCount - existingParcels.size());
            for (int i = existingParcels.size(); i < parcelCount; i++) {
                createSimulatedParcel(i);
            }
        }

        // Initialize routes for all in-transit parcels
        List<Parcel> inTransitParcels = parcelRepository.findByStatus(Parcel.ParcelStatus.IN_TRANSIT);
        for (Parcel parcel : inTransitParcels) {
            if (parcel.getCurrentLatitude() != null && parcel.getCurrentLongitude() != null &&
                    parcel.getDestinationLatitude() != null && parcel.getDestinationLongitude() != null) {
                activeRoutes.put(parcel.getId(), new SimulatedRoute(
                        parcel.getCurrentLongitude(),
                        parcel.getCurrentLatitude(),
                        parcel.getDestinationLongitude(),
                        parcel.getDestinationLatitude()));
            }
        }

        log.info("Initialized {} active routes for simulation", activeRoutes.size());
    }

    private void createSimulatedParcel(int index) {
        Parcel parcel = new Parcel();
        parcel.setTrackingNumber("RM-2026-" + String.format("%06d", index + 1));
        parcel.setStatus(Parcel.ParcelStatus.IN_TRANSIT);

        // Random origin
        double originLon = CENTER_LON + (random.nextDouble() - 0.5) * RADIUS;
        double originLat = CENTER_LAT + (random.nextDouble() - 0.5) * RADIUS;
        parcel.setOriginLongitude(originLon);
        parcel.setOriginLatitude(originLat);
        parcel.setCurrentLongitude(originLon);
        parcel.setCurrentLatitude(originLat);

        // Random destination
        double destLon = CENTER_LON + (random.nextDouble() - 0.5) * RADIUS;
        double destLat = CENTER_LAT + (random.nextDouble() - 0.5) * RADIUS;
        parcel.setDestinationLongitude(destLon);
        parcel.setDestinationLatitude(destLat);

        parcel.setAssignedDriverId("driver-" + (index % 20 + 1));
        parcel.setAssignedVehicleId("vehicle-" + (index % 50 + 1));
        parcel.setRecipientName("Customer " + (index + 1));
        parcel.setWeight(random.nextDouble() * 10 + 0.5);
        parcel.setPriority(random.nextBoolean() ? "HIGH" : "MEDIUM");

        parcelRepository.save(parcel);
    }

    /**
     * Simulate location updates every second
     */
    @Scheduled(fixedDelayString = "${app.simulator.interval:1000}")
    public void simulateLocationUpdates() {
        if (!simulatorEnabled || activeRoutes.isEmpty()) {
            return;
        }

        activeRoutes.forEach((parcelId, route) -> {
            // Move parcel towards destination
            route.advance();

            // Update in database
            parcelRepository.findById(parcelId).ifPresent(parcel -> {
                parcel.setCurrentLongitude(route.currentLon);
                parcel.setCurrentLatitude(route.currentLat);
                parcelRepository.save(parcel);

                // Push update via WebSocket
                LocationUpdate update = new LocationUpdate(
                        parcelId,
                        parcel.getTrackingNumber(),
                        route.currentLon,
                        route.currentLat,
                        parcel.getStatus().name(),
                        System.currentTimeMillis());

                messagingTemplate.convertAndSend("/topic/locations", update);
                messagingTemplate.convertAndSend("/topic/locations/" + parcelId, update);
            });

            // Check if reached destination
            if (route.hasReachedDestination()) {
                parcelRepository.findById(parcelId).ifPresent(parcel -> {
                    parcel.setStatus(Parcel.ParcelStatus.DELIVERED);
                    parcelRepository.save(parcel);
                    activeRoutes.remove(parcelId);
                    log.info("Parcel {} reached destination", parcel.getTrackingNumber());
                });
            }
        });
    }

    /**
     * Simulated route that moves a parcel from origin to destination
     */
    private static class SimulatedRoute {
        private double currentLon;
        private double currentLat;
        private final double destLon;
        private final double destLat;
        private static final double STEP_SIZE = 0.001; // ~111 meters per step

        public SimulatedRoute(double startLon, double startLat, double destLon, double destLat) {
            this.currentLon = startLon;
            this.currentLat = startLat;
            this.destLon = destLon;
            this.destLat = destLat;
        }

        public void advance() {
            double deltaLon = destLon - currentLon;
            double deltaLat = destLat - currentLat;
            double distance = Math.sqrt(deltaLon * deltaLon + deltaLat * deltaLat);

            if (distance > STEP_SIZE) {
                currentLon += (deltaLon / distance) * STEP_SIZE;
                currentLat += (deltaLat / distance) * STEP_SIZE;
            } else {
                currentLon = destLon;
                currentLat = destLat;
            }
        }

        public boolean hasReachedDestination() {
            double distance = Math.sqrt(
                    Math.pow(destLon - currentLon, 2) + Math.pow(destLat - currentLat, 2));
            return distance < STEP_SIZE;
        }
    }

    public record LocationUpdate(
            String parcelId,
            String trackingNumber,
            double longitude,
            double latitude,
            String status,
            long timestamp) {
    }
}
