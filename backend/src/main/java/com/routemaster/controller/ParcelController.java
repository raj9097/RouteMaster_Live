package com.routemaster.controller;

import com.routemaster.model.Parcel;
import com.routemaster.service.ParcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parcels")
@RequiredArgsConstructor
public class ParcelController {

    private final ParcelService parcelService;

    @GetMapping
    public ResponseEntity<List<Parcel>> getAllParcels() {
        return ResponseEntity.ok(parcelService.getAllParcels());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Parcel> getParcelById(@PathVariable String id) {
        return parcelService.getParcelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<Parcel> getParcelByTrackingNumber(@PathVariable String trackingNumber) {
        return parcelService.getParcelByTrackingNumber(trackingNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Parcel>> getParcelsByStatus(@PathVariable Parcel.ParcelStatus status) {
        return ResponseEntity.ok(parcelService.getParcelsByStatus(status));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Parcel>> getParcelsByDriver(@PathVariable String driverId) {
        return ResponseEntity.ok(parcelService.getParcelsByDriver(driverId));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Parcel>> findParcelsNearby(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {
        return ResponseEntity.ok(parcelService.findParcelsNearLocation(longitude, latitude, radiusKm));
    }

    @PostMapping
    public ResponseEntity<Parcel> createParcel(@RequestBody Parcel parcel) {
        return ResponseEntity.ok(parcelService.createParcel(parcel));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Parcel> updateParcel(@PathVariable String id, @RequestBody Parcel parcel) {
        return ResponseEntity.ok(parcelService.updateParcel(id, parcel));
    }

    @PatchMapping("/{id}/location")
    public ResponseEntity<Void> updateLocation(
            @PathVariable String id,
            @RequestParam double longitude,
            @RequestParam double latitude) {
        parcelService.updateParcelLocation(id, longitude, latitude);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParcel(@PathVariable String id) {
        parcelService.deleteParcel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/count")
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(new StatsResponse(
                parcelService.countParcelsByStatus(Parcel.ParcelStatus.PENDING),
                parcelService.countParcelsByStatus(Parcel.ParcelStatus.IN_TRANSIT),
                parcelService.countParcelsByStatus(Parcel.ParcelStatus.DELIVERED),
                parcelService.countParcelsByStatus(Parcel.ParcelStatus.FAILED)));
    }

    public record StatsResponse(long pending, long inTransit, long delivered, long failed) {
    }
}
