package com.example.carpark.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for converting coordinates between different coordinate systems
 * Currently supports SVY21 to WGS84 conversion with configurable parameters
 */
@Service
public class CoordinateConversionService {

    private static final Logger logger = LoggerFactory.getLogger(CoordinateConversionService.class);

    // Origin coordinates for the coordinate system
    @Value("${carpark.coordinate.origin.lat:1.3666666666666667}")
    private double originLat;

    @Value("${carpark.coordinate.origin.lon:103.83333333333333}")
    private double originLon;

    @Value("${carpark.coordinate.origin.n:38744.572}")
    private double originN;

    @Value("${carpark.coordinate.origin.e:28001.642}")
    private double originE;

    // Conversion factors for coordinate transformation
    @Value("${carpark.coordinate.factor.lat:0.0000089831}")
    private double latFactor;

    @Value("${carpark.coordinate.factor.lon:0.0000111319}")
    private double lonFactor;

    // Coordinate bounds validation
    @Value("${carpark.coordinate.bounds.lat.min:-90.0}")
    private double latMin;

    @Value("${carpark.coordinate.bounds.lat.max:90.0}")
    private double latMax;

    @Value("${carpark.coordinate.bounds.lon.min:-180.0}")
    private double lonMin;

    @Value("${carpark.coordinate.bounds.lon.max:180.0}")
    private double lonMax;

    /**
     * Convert SVY21 coordinates to WGS84 using configurable mathematical
     * transformation
     * This method can be extended to support different coordinate systems by
     * configuring
     * the appropriate origin points and conversion factors
     */
    public BigDecimal[] convertSVY21ToWGS84(BigDecimal svy21X, BigDecimal svy21Y) {
        try {
            // Calculate relative coordinates
            double dN = svy21Y.doubleValue() - originN;
            double dE = svy21X.doubleValue() - originE;

            // Calculate latitude and longitude using floating point arithmetic
            double lat = originLat + (dN * latFactor);
            double lon = originLon + (dE * lonFactor);

            // Validate coordinates are within configured bounds
            if (isOutOfBounds(lat, lon)) {
                logger.warn("Converted coordinates out of bounds: lat={}, lon={}", lat, lon);
                throw new IllegalArgumentException("Coordinates out of bounds");
            }

            return new BigDecimal[] {
                    BigDecimal.valueOf(lat).setScale(8, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(lon).setScale(8, RoundingMode.HALF_UP)
            };
        } catch (Exception e) {
            logger.error("Error in coordinate conversion: X={}, Y={}", svy21X, svy21Y, e);
            // Return default coordinates (can be configured)
            return new BigDecimal[] {
                    new BigDecimal("1.3521"),
                    new BigDecimal("103.8198")
            };
        }
    }

    /**
     * Check if coordinates are within configured bounds
     * This can be configured per region or coordinate system
     */
    private boolean isOutOfBounds(double lat, double lon) {
        // Use configurable bounds for coordinate validation
        return lat < latMin || lat > latMax || lon < lonMin || lon > lonMax;
    }

    /**
     * Get the current coordinate conversion configuration
     * Useful for debugging and monitoring
     */
    public String getConfigurationInfo() {
        return String.format(
                "Coordinate Conversion Config - Origin: (%.8f, %.8f), Factors: (%.10f, %.10f), Bounds: Lat[%.2f, %.2f], Lon[%.2f, %.2f]",
                originLat, originLon, latFactor, lonFactor, latMin, latMax, lonMin, lonMax);
    }
}
