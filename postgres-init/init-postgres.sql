-- PostgreSQL initialization script for analytics database

-- Create route_analytics table (Spring Batch will create batch metadata tables)
CREATE TABLE IF NOT EXISTS route_analytics (
    id BIGSERIAL PRIMARY KEY,
    route_id VARCHAR(255) NOT NULL,
    driver_id VARCHAR(255) NOT NULL,
    vehicle_id VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    total_distance DOUBLE PRECISION,
    average_speed DOUBLE PRECISION,
    max_speed DOUBLE PRECISION,
    total_stops INTEGER,
    deliveries_completed INTEGER,
    deliveries_failed INTEGER,
    fuel_efficiency DOUBLE PRECISION,
    total_duration_minutes BIGINT,
    processed_at TIMESTAMP,
    notes VARCHAR(500),
    CONSTRAINT uk_route_date UNIQUE (route_id, date)
);

-- Create indexes for better query performance
CREATE INDEX idx_route_analytics_date ON route_analytics(date);
CREATE INDEX idx_route_analytics_driver ON route_analytics(driver_id);
CREATE INDEX idx_route_analytics_vehicle ON route_analytics(vehicle_id);

-- Insert sample analytics data for testing
INSERT INTO route_analytics (route_id, driver_id, vehicle_id, date, total_distance, average_speed, max_speed, total_stops, deliveries_completed, deliveries_failed, fuel_efficiency, total_duration_minutes, processed_at)
VALUES 
    ('route-001', 'driver-1', 'vehicle-1', CURRENT_DATE - INTERVAL '1 day', 125.5, 45.2, 80.0, 12, 15, 0, 12.5, 180, NOW()),
    ('route-002', 'driver-2', 'vehicle-2', CURRENT_DATE - INTERVAL '1 day', 98.3, 42.8, 75.0, 10, 12, 1, 11.8, 150, NOW()),
    ('route-003', 'driver-3', 'vehicle-3', CURRENT_DATE - INTERVAL '1 day', 156.7, 48.5, 85.0, 15, 18, 0, 13.2, 210, NOW());

-- Create a view for daily summary statistics
CREATE OR REPLACE VIEW daily_analytics_summary AS
SELECT 
    date,
    COUNT(*) as total_routes,
    SUM(deliveries_completed) as total_deliveries,
    SUM(deliveries_failed) as total_failures,
    AVG(average_speed) as avg_speed,
    SUM(total_distance) as total_distance,
    AVG(fuel_efficiency) as avg_fuel_efficiency
FROM route_analytics
GROUP BY date
ORDER BY date DESC;

-- Grant permissions (if needed)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO analytics;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO analytics;

SELECT 'PostgreSQL analytics database initialized successfully' AS status;
