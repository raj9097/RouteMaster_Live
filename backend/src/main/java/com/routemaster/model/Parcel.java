package com.routemaster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "parcels")
@EntityListeners(AuditingEntityListener.class)
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private ParcelStatus status;

    // Current location (simplified from GeoJsonPoint)
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

    @Column(length = 500)
    private String recipientAddress;

    private Double weight; // in kg

    private String priority; // LOW, MEDIUM, HIGH, URGENT

    @CreatedDate
    @Column(updatable = false)
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
