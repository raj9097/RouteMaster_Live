package com.routemaster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "route_logs")
public class RouteLog {

    @Id
    private String id;

    private String routeId;

    private String driverId;

    private String vehicleId;

    private LocalDate date;

    private List<LocationPoint> locations = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationPoint {
        private LocalDateTime timestamp;
        private Double longitude;
        private Double latitude;
        private Double speed; // km/h
        private Double heading; // degrees
    }
}
