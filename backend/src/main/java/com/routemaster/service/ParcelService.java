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
        // ID not manually set -> Mongo generates it
        Parcel saved = parcelRepository.save(parcel);
        log.info("Created parcel: {}", saved.getTrackingNumber());

        // Broadcast new parcel to all subscribers
        messagingTemplate.convertAndSend("/topic/parcels", saved);

        return saved;
    }

    public Optional<Parcel> getParcelById(String id) {
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

    public Parcel updateParcel(String id, Parcel parcel) {
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

    public void updateParcelLocation(String id, double longitude, double latitude) {
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

    public void deleteParcel(String id) {
        parcelRepository.deleteById(id);
        log.info("Deleted parcel: {}", id);
    }

    /**
     * Geospatial query logic.
     * Note: For advanced geospatial queries, you should use Mongo geospatial
     * indexes and Criteria queries.
     * Here we can return a simple list or re-enable the logic if moving to proper
     * GeoJSON types.
     * For now, returning all for simplicity or implementing basic Haversine
     * distance in memory if dataset is small,
     * BUT since we migrated to Mongo, we can use MongoRepository's native
     * geospatial capabilities if we used GeoJsonPoint.
     * Given the current Parcel model uses lat/lon doubles, we'll keep it simple or
     * fix it later.
     */
    public List<Parcel> findParcelsNearLocation(double longitude, double latitude, double radiusKm) {
        // TODO: Implement proper geospatial search using MongoTemplate or switch Parcel
        // to use GeoJsonPoint
        log.info("Finding parcels near [{}, {}]", longitude, latitude);
        // Temporary: return all parcels (filtering should be done via query)
        return parcelRepository.findAll();
    }

    public long countParcelsByStatus(Parcel.ParcelStatus status) {
        return parcelRepository.countByStatus(status);
    }

    public record LocationUpdate(String parcelId, double longitude, double latitude, long timestamp) {
    }
}
