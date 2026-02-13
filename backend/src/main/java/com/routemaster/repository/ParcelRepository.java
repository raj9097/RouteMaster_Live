package com.routemaster.repository;

import com.routemaster.model.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Long> {

    Optional<Parcel> findByTrackingNumber(String trackingNumber);

    List<Parcel> findByStatus(Parcel.ParcelStatus status);

    List<Parcel> findByAssignedDriverId(String driverId);

    long countByStatus(Parcel.ParcelStatus status);

    // Note: Geospatial queries removed - not supported by H2
    // For simple location queries, you can use custom queries with lat/lon
    // calculations
}
