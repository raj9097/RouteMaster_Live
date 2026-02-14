package com.routemaster.repository;

import com.routemaster.model.RouteAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteAnalyticsRepository extends MongoRepository<RouteAnalytics, String> {
}
