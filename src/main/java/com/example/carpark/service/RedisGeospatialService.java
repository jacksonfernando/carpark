package com.example.carpark.service;

import com.example.carpark.entity.CarPark;
import com.example.carpark.repository.mysql.CarParkMySqlRepository;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisGeospatialService {

    private static final Logger logger = LoggerFactory.getLogger(RedisGeospatialService.class);
    private static final String CAR_PARK_LOCATIONS_KEY = "carpark:locations";
    private static final int CACHE_TTL_MINUTES = 15;

    private final RedisTemplate<String, Object> redisTemplate;
    private final CarParkMySqlRepository carParkMySqlRepository;

    public RedisGeospatialService(RedisTemplate<String, Object> redisTemplate,
            CarParkMySqlRepository carParkMySqlRepository) {
        this.redisTemplate = redisTemplate;
        this.carParkMySqlRepository = carParkMySqlRepository;
    }

    /**
     * Cache car park locations during data import
     */
    public void cacheCarParkLocations(List<CarPark> carParks) {
        try {
            logger.info("Caching {} car park locations", carParks.size());

            for (CarPark carPark : carParks) {
                if (carPark.getLatitude() != null && carPark.getLongitude() != null) {
                    String member = carPark.getCarParkNo();
                    double latitude = carPark.getLatitude().doubleValue();
                    double longitude = carPark.getLongitude().doubleValue();

                    redisTemplate.opsForGeo().add(CAR_PARK_LOCATIONS_KEY,
                            new org.springframework.data.geo.Point(longitude, latitude), member);
                }
            }

            redisTemplate.expire(CAR_PARK_LOCATIONS_KEY, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            logger.info("Successfully cached {} car park locations", carParks.size());

        } catch (Exception e) {
            logger.error("Error caching car park locations", e);
        }
    }

    /**
     * Find nearby car parks using Redis geospatial queries with database fallback
     */
    public List<CarPark> findNearbyCarParks(BigDecimal latitude, BigDecimal longitude,
            int radiusKm, int limit) {
        try {
            var results = redisTemplate.opsForGeo().radius(CAR_PARK_LOCATIONS_KEY,
                    new org.springframework.data.geo.Point(longitude.doubleValue(), latitude.doubleValue()),
                    radiusKm * 1000); // Convert km to meters

            if (results != null && results.getContent() != null && !results.getContent().isEmpty()) {
                logger.debug("Found {} car parks in Redis cache within {}km", results.getContent().size(), radiusKm);

                List<CarPark> nearbyCarParks = new ArrayList<>();
                for (var geoResult : results.getContent()) {
                    String carParkNo = (String) geoResult.getContent().getName();

                    CarPark carPark = carParkMySqlRepository.findByCarParkNoAndDeletedAtIsNull(carParkNo)
                            .orElse(null);
                    if (carPark != null && carPark.getAvailableLots() > 0) {
                        nearbyCarParks.add(carPark);
                        if (nearbyCarParks.size() >= limit) {
                            break;
                        }
                    }
                }

                if (!nearbyCarParks.isEmpty()) {
                    logger.debug("Returning {} car parks from Redis cache + database", nearbyCarParks.size());
                    return nearbyCarParks;
                }
            }

        } catch (Exception e) {
            logger.warn("Redis geospatial query failed, falling back to database", e);
        }

        logger.debug("Falling back to database query for nearby car parks");
        return carParkMySqlRepository.findNearestCarParksWithPoint(
                createPoint(latitude, longitude), limit, 0);
    }

    /**
     * Refresh location cache every 15 minutes
     */
    public void refreshLocationCache() {
        try {
            logger.info("Refreshing location cache");

            redisTemplate.delete(CAR_PARK_LOCATIONS_KEY);
            List<CarPark> allCarParks = carParkMySqlRepository.findAll();
            cacheCarParkLocations(allCarParks);

            logger.info("Location cache refresh completed");

        } catch (Exception e) {
            logger.error("Error refreshing location cache", e);
        }
    }

    /**
     * Create a JTS Point from coordinates
     */
    private Point createPoint(BigDecimal latitude, BigDecimal longitude) {
        org.locationtech.jts.geom.GeometryFactory factory = new org.locationtech.jts.geom.GeometryFactory();
        Point point = factory.createPoint(new org.locationtech.jts.geom.Coordinate(
                longitude.doubleValue(), latitude.doubleValue()));
        point.setSRID(4326);
        return point;
    }
}
