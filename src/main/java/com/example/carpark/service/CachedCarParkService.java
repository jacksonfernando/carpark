package com.example.carpark.service;

import com.example.carpark.dto.request.NearestCarParkRequestDTO;
import com.example.carpark.dto.response.CarParkResponseDTO;
import com.example.carpark.entity.CarPark;
import com.example.carpark.repository.mysql.CarParkMySqlRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CachedCarParkService {

    private static final Logger logger = LoggerFactory.getLogger(
        CachedCarParkService.class
    );

    private final CarParkMySqlRepository carParkMySqlRepository;
    private final GeometryFactory geometryFactory;
    private final RedisGeospatialService redisGeospatialService;

    public CachedCarParkService(
        CarParkMySqlRepository carParkMySqlRepository,
        GeometryFactory geometryFactory,
        RedisGeospatialService redisGeospatialService
    ) {
        this.carParkMySqlRepository = carParkMySqlRepository;
        this.geometryFactory = geometryFactory;
        this.redisGeospatialService = redisGeospatialService;
    }

    /**
     * Find nearest car parks using Redis geospatial cache with database fallback
     */
    public List<CarParkResponseDTO> findNearestCarParks(
        NearestCarParkRequestDTO request
    ) {
        try {
            logger.debug(
                "Finding nearest car parks for coordinates: {}, {}",
                request.getLatitude(),
                request.getLongitude()
            );

            // Try Redis geospatial cache first
            List<CarPark> cachedResults =
                redisGeospatialService.findNearbyCarParks(
                    request.getLatitude(),
                    request.getLongitude(),
                    50, // 50km radius for initial search
                    request.getPerPage()
                );

            if (cachedResults != null && !cachedResults.isEmpty()) {
                logger.debug(
                    "Found {} car parks in Redis cache",
                    cachedResults.size()
                );
                return convertToResponseDTO(cachedResults);
            }

            // Fallback to database query
            logger.debug("Cache miss, querying database for nearest car parks");
            Point searchPoint = createSearchPoint(
                request.getLatitude(),
                request.getLongitude()
            );

            List<CarPark> carParks =
                carParkMySqlRepository.findNearestCarParksWithPoint(
                    searchPoint,
                    request.getPerPage(),
                    (request.getPage() - 1) * request.getPerPage()
                );

            return convertToResponseDTO(carParks);
        } catch (Exception e) {
            logger.error("Error finding nearest car parks", e);
            throw new RuntimeException("Failed to find nearest car parks", e);
        }
    }

    /**
     * Get all car parks with availability (cached)
     */
    @Cacheable(value = "carParks", key = "'allWithAvailability'")
    public List<CarParkResponseDTO> getAllCarParksWithAvailability() {
        logger.debug("Getting all car parks with availability from cache");

        List<CarPark> carParks =
            carParkMySqlRepository.findCarParksWithAvailability();
        return convertToResponseDTO(carParks);
    }

    /**
     * Find nearest car parks with limit (cached)
     */
    public List<CarParkResponseDTO> findNearestCarParksWithLimit(
        BigDecimal latitude,
        BigDecimal longitude,
        int limit
    ) {
        try {
            logger.debug(
                "Finding nearest {} car parks for coordinates: {}, {}",
                limit,
                latitude,
                longitude
            );

            // Try Redis geospatial cache first
            List<CarPark> cachedResults =
                redisGeospatialService.findNearbyCarParks(
                    latitude,
                    longitude,
                    50,
                    limit
                );

            if (cachedResults != null && !cachedResults.isEmpty()) {
                logger.debug(
                    "Found {} car parks in Redis cache",
                    cachedResults.size()
                );
                return convertToResponseDTO(cachedResults);
            }

            // Fallback to database query
            logger.debug(
                "Cache miss, querying database for nearest car parks with limit"
            );
            Point searchPoint = createSearchPoint(latitude, longitude);

            List<CarPark> carParks =
                carParkMySqlRepository.findNearestCarParksWithPoint(
                    searchPoint,
                    limit,
                    0
                );

            return convertToResponseDTO(carParks);
        } catch (Exception e) {
            logger.error("Error finding nearest car parks with limit", e);
            throw new RuntimeException(
                "Failed to find nearest car parks with limit",
                e
            );
        }
    }

    /**
     * Create a JTS Point for database queries
     */
    private Point createSearchPoint(BigDecimal latitude, BigDecimal longitude) {
        Point point = geometryFactory.createPoint(
            new Coordinate(longitude.doubleValue(), latitude.doubleValue())
        );
        point.setSRID(4326);
        return point;
    }

    /**
     * Convert CarPark entities to response DTOs
     */
    private List<CarParkResponseDTO> convertToResponseDTO(
        List<CarPark> carParks
    ) {
        return carParks
            .stream()
            .map(carPark ->
                new CarParkResponseDTO(
                    carPark.getAddress(),
                    carPark.getLatitude(),
                    carPark.getLongitude(),
                    carPark.getTotalLots(),
                    carPark.getAvailableLots()
                )
            )
            .collect(Collectors.toList());
    }
}
