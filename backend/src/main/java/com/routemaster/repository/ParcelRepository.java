package com.routemaster.repository;

import com.routemaster.model.Parcel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelRepository extends MongoRepository<Parcel, String> {

    Optional<Parcel> findByTrackingNumber(String trackingNumber);

    List<Parcel> findByStatus(Parcel.ParcelStatus status);

    List<Parcel> findByAssignedDriverId(String driverId);

    long countByStatus(Parcel.ParcelStatus status);
}
