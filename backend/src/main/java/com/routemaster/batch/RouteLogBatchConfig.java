package com.routemaster.batch;

import com.routemaster.model.RouteAnalytics;
import com.routemaster.model.RouteLog;
import com.routemaster.repository.RouteAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import com.routemaster.repository.RouteLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RouteLogBatchConfig {

    private final RouteLogRepository routeLogRepository;
    private final RouteAnalyticsRepository routeAnalyticsRepository;

    @Value("${app.batch.route-log-processor.chunk-size:1000}")
    private int chunkSize;

    /**
     * Reader: Read route logs from H2 database using JPA
     */
    @Bean
    public RepositoryItemReader<RouteLog> routeLogReader() {
        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("id", Sort.Direction.ASC);

        return new RepositoryItemReaderBuilder<RouteLog>()
                .name("routeLogReader")
                .repository(routeLogRepository)
                .methodName("findByDate")
                .arguments(LocalDate.now().minusDays(1)) // Yesterday's logs
                .sorts(sorts)
                .build();
    }

    /**
     * Processor: Calculate analytics from route logs
     */
    @Bean
    public ItemProcessor<RouteLog, RouteAnalytics> routeLogProcessor() {
        return routeLog -> {
            RouteAnalytics analytics = new RouteAnalytics();
            analytics.setRouteId(routeLog.getRouteId());
            analytics.setDriverId(routeLog.getDriverId());
            analytics.setVehicleId(routeLog.getVehicleId());
            analytics.setDate(routeLog.getDate());

            if (routeLog.getLocations() != null && !routeLog.getLocations().isEmpty()) {
                // Calculate total distance
                double totalDistance = calculateTotalDistance(routeLog.getLocations());
                analytics.setTotalDistance(totalDistance);

                // Calculate average speed
                double avgSpeed = routeLog.getLocations().stream()
                        .filter(loc -> loc.getSpeed() != null)
                        .mapToDouble(RouteLog.LocationPoint::getSpeed)
                        .average()
                        .orElse(0.0);
                analytics.setAverageSpeed(avgSpeed);

                // Calculate max speed
                double maxSpeed = routeLog.getLocations().stream()
                        .filter(loc -> loc.getSpeed() != null)
                        .mapToDouble(RouteLog.LocationPoint::getSpeed)
                        .max()
                        .orElse(0.0);
                analytics.setMaxSpeed(maxSpeed);

                // Calculate duration
                if (routeLog.getLocations().size() > 1) {
                    LocalDateTime start = routeLog.getLocations().get(0).getTimestamp();
                    LocalDateTime end = routeLog.getLocations().get(routeLog.getLocations().size() - 1).getTimestamp();
                    long durationMinutes = java.time.Duration.between(start, end).toMinutes();
                    analytics.setTotalDurationMinutes(durationMinutes);
                }

                // Estimate fuel efficiency (simplified formula)
                if (totalDistance > 0 && avgSpeed > 0) {
                    analytics.setFuelEfficiency(totalDistance / (avgSpeed * 0.1)); // Simplified
                }
            }

            analytics.setTotalStops(0); // Would need additional logic
            analytics.setDeliveriesCompleted(0); // Would need parcel data
            analytics.setDeliveriesFailed(0);
            analytics.setProcessedAt(LocalDateTime.now());

            log.debug("Processed route log: {} -> avg speed: {}", routeLog.getRouteId(), analytics.getAverageSpeed());

            return analytics;
        };
    }

    /**
     * Writer: Write analytics to PostgreSQL
     */
    @Bean
    public ItemWriter<RouteAnalytics> routeAnalyticsWriter() {
        return items -> {
            routeAnalyticsRepository.saveAll(items);
            log.info("Wrote {} route analytics records to PostgreSQL", items.size());
        };
    }

    /**
     * Step: Process route logs
     */
    @Bean
    public Step processRouteLogsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("processRouteLogsStep", jobRepository)
                .<RouteLog, RouteAnalytics>chunk(chunkSize, transactionManager)
                .reader(routeLogReader())
                .processor(routeLogProcessor())
                .writer(routeAnalyticsWriter())
                .build();
    }

    /**
     * Job: Route log processing job
     */
    @Bean
    public Job routeLogProcessingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("routeLogProcessingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(processRouteLogsStep(jobRepository, transactionManager))
                .build();
    }

    /**
     * Calculate total distance using Haversine formula
     */
    private double calculateTotalDistance(java.util.List<RouteLog.LocationPoint> locations) {
        double totalDistance = 0.0;

        for (int i = 1; i < locations.size(); i++) {
            RouteLog.LocationPoint prev = locations.get(i - 1);
            RouteLog.LocationPoint curr = locations.get(i);

            totalDistance += haversineDistance(
                    prev.getLatitude(), prev.getLongitude(),
                    curr.getLatitude(), curr.getLongitude());
        }

        return totalDistance;
    }

    /**
     * Haversine formula for distance between two points (in kilometers)
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
