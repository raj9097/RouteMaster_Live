package com.routemaster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "route_analytics")
public class RouteAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String routeId;
    
    @Column(nullable = false)
    private String driverId;
    
    @Column(nullable = false)
    private String vehicleId;
    
    @Column(nullable = false)
    private LocalDate date;
    
    private Double totalDistance;  // km
    
    private Double averageSpeed;  // km/h
    
    private Double maxSpeed;  // km/h
    
    private Integer totalStops;
    
    private Integer deliveriesCompleted;
    
    private Integer deliveriesFailed;
    
    private Double fuelEfficiency;  // km/liter (estimated)
    
    private Long totalDurationMinutes;
    
    private LocalDateTime processedAt;
    
    @Column(length = 500)
    private String notes;
}
