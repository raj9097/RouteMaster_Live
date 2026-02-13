package com.routemaster.service;

import com.routemaster.model.Parcel;
import com.routemaster.repository.ParcelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParcelService {

    private final ParcelRepository parcelRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Parcel createParcel(Parcel parcel) {
        Parcel saved = parcelRepository.save(parcel);
        log.info("Created parcel: {}", saved.getTrackingNumber());

        // Broadcast new parcel to all subscribers
        messagingTemplate.convertAndSend("/topic/parcels", saved);

        return saved;
    }

    public Optional<Parcel> getParcelById(Long id) {
        return parcelRepository.findById(id);
    }

    public Optional<Parcel> getParcelByTrackingNumber(String trackingNumber) {
        return parcelRepository.findByTrackingNumber(trackingNumber);
    }

    public List<Parcel> getAllParcels() {
        return parcelRepository.findAll();
    }

    public List<Parcel> getParcelsByStatus(Parcel.ParcelStatus status) {
        return parcelRepository.findByStatus(status);
    }

    public List<Parcel> getParcelsByDriver(String driverId) {
        return parcelRepository.findByAssignedDriverId(driverId);
    }

    public Parcel updateParcel(Long id, Parcel parcel) {
        return parcelRepository.findById(id)
                .map(existing -> {
                    parcel.setId(existing.getId());
                    Parcel updated = parcelRepository.save(parcel);

                    // Broadcast update to all subscribers
                    messagingTemplate.convertAndSend("/topic/parcels", updated);

                    // Also send to specific parcel channel
                    messagingTemplate.convertAndSend("/topic/parcels/" + updated.getId(), updated);

                    log.info("Updated parcel: {}", updated.getTrackingNumber());
                    return updated;
                })
                .orElseThrow(() -> new RuntimeException("Parcel not found: " + id));
    }

    public void updateParcelLocation(Long id, double longitude, double latitude) {
        parcelRepository.findById(id).ifPresent(parcel -> {
            parcel.setCurrentLongitude(longitude);
            parcel.setCurrentLatitude(latitude);
            Parcel updated = parcelRepository.save(parcel);

            // Push location update via WebSocket
            messagingTemplate.convertAndSend("/topic/locations/" + id,
                    new LocationUpdate(id, longitude, latitude, System.currentTimeMillis()));

            log.debug("Updated location for parcel {}: [{}, {}]", id, longitude, latitude);
        });
    }

    public void deleteParcel(Long id) {
        parcelRepository.deleteById(id);
        log.info("Deleted parcel: {}", id);
    }

    /**
     * Note: Geospatial queries removed - not supported by H2
     * For production, use MongoDB or PostGIS for advanced geospatial features
     */
    public List<Parcel> findParcelsNearLocation(double longitude, double latitude, double radiusKm) {
        log.warn("Geospatial queries not supported with H2 - returning empty list");
        return List.of();
    }

    public long countParcelsByStatus(Parcel.ParcelStatus status) {
        return parcelRepository.countByStatus(status);
    }

    // DTO for location updates
    public record LocationUpdate(Long parcelId, double longitude, double latitude, long timestamp) {
    }
}
