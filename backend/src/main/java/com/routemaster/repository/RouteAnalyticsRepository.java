package com.routemaster.repository;

import com.routemaster.model.RouteAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RouteAnalyticsRepository extends JpaRepository<RouteAnalytics, Long> {
    
    List<RouteAnalytics> findByDate(LocalDate date);
    
    List<RouteAnalytics> findByDriverId(String driverId);
    
    List<RouteAnalytics> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT AVG(r.averageSpeed) FROM RouteAnalytics r WHERE r.date = :date")
    Double getAverageSpeedForDate(LocalDate date);
    
    @Query("SELECT SUM(r.deliveriesCompleted) FROM RouteAnalytics r WHERE r.date = :date")
    Long getTotalDeliveriesForDate(LocalDate date);
}
