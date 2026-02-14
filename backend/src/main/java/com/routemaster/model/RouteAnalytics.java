package com.routemaster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "route_analytics")
public class RouteAnalytics {

    @Id
    private String id;

    private String routeId;

    private String driverId;

    private String vehicleId;

    private LocalDate date;

    private Double totalDistance; // km

    private Double averageSpeed; // km/h

    private Double maxSpeed; // km/h

    private Integer totalStops;

    private Integer deliveriesCompleted;

    private Integer deliveriesFailed;

    private Double fuelEfficiency; // km/liter (estimated)

    private Long totalDurationMinutes;

    private LocalDateTime processedAt;

    private String notes;
}
