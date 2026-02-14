package com.routemaster.repository;

import com.routemaster.model.RouteLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteLogRepository extends MongoRepository<RouteLog, String> {
}
