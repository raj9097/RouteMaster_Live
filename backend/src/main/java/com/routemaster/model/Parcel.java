package com.routemaster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "parcels")
public class Parcel {

    @Id
    private String id;

    private String trackingNumber;

    private ParcelStatus status;

    // Current location
    private Double currentLatitude;
    private Double currentLongitude;

    // Origin location
    private Double originLatitude;
    private Double originLongitude;

    // Destination location
    private Double destinationLatitude;
    private Double destinationLongitude;

    private String assignedDriverId;

    private String assignedVehicleId;

    private LocalDateTime estimatedDelivery;

    private String recipientName;

    private String recipientPhone;

    private String recipientAddress;

    private Double weight; // in kg

    private String priority; // LOW, MEDIUM, HIGH, URGENT

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime lastUpdated;

    public enum ParcelStatus {
        PENDING,
        PICKED_UP,
        IN_TRANSIT,
        OUT_FOR_DELIVERY,
        DELIVERED,
        FAILED,
        RETURNED
    }
}
