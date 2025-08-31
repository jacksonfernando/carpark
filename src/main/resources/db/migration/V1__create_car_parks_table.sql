-- Create car_parks table with spatial location field
CREATE TABLE car_parks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_park_no VARCHAR(50) NOT NULL UNIQUE,
    address TEXT NOT NULL,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    location POINT SRID 4326 NOT NULL,
    total_lots INT DEFAULT 0,
    available_lots INT DEFAULT 0,
    car_park_type VARCHAR(100),
    type_of_parking_system VARCHAR(100),
    short_term_parking VARCHAR(100),
    free_parking VARCHAR(100),
    night_parking VARCHAR(100),
    car_park_decks VARCHAR(50),
    gantry_height VARCHAR(50),
    car_park_basement VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) DEFAULT 'SYSTEM',
    deleted_at TIMESTAMP NULL
);

-- Create spatial index for efficient distance calculations
CREATE SPATIAL INDEX idx_location ON car_parks (location);

-- Create indexes for common query patterns
CREATE INDEX idx_car_park_no ON car_parks (car_park_no);
CREATE INDEX idx_available_lots ON car_parks (available_lots);
CREATE INDEX idx_deleted_at ON car_parks (deleted_at);
CREATE INDEX idx_created_at ON car_parks (created_at);
CREATE INDEX idx_updated_at ON car_parks (updated_at);
CREATE INDEX idx_latitude_longitude ON car_parks (latitude, longitude);
CREATE INDEX idx_car_park_type ON car_parks (car_park_type);
CREATE INDEX idx_type_of_parking_system ON car_parks (type_of_parking_system);

-- Create composite indexes for optimized queries
CREATE INDEX idx_available_deleted ON car_parks (available_lots, deleted_at);
CREATE INDEX idx_deleted_available ON car_parks (deleted_at, available_lots);

-- Create a view for available car parks (with lots > 0)
CREATE VIEW available_car_parks AS
SELECT * FROM car_parks
WHERE available_lots > 0 AND deleted_at IS NULL;
