package com.routemaster.repository;

import com.routemaster.model.RouteLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RouteLogRepository extends JpaRepository<RouteLog, Long> {

    List<RouteLog> findByDate(LocalDate date);

    List<RouteLog> findByDriverId(String driverId);

    List<RouteLog> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
