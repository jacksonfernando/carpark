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

    public CachedCarParkService(
        CarParkMySqlRepository carParkMySqlRepository,
        GeometryFactory geometryFactory
    ) {
        this.carParkMySqlRepository = carParkMySqlRepository;
        this.geometryFactory = geometryFactory;
    }

    /**
     * Find nearest car parks with caching using spatial functions
     */
    @Cacheable(
        value = "nearestCarParks",
        key = "#request.latitude + '_' + #request.longitude + '_' + #request.page + '_' + #request.perPage"
    )
    public List<CarParkResponseDTO> findNearestCarParks(
        NearestCarParkRequestDTO request
    ) {
        logger.info(
            "Cache miss - fetching nearest car parks for lat: {}, lon: {}, page: {}",
            request.getLatitude(),
            request.getLongitude(),
            request.getPage()
        );

        try {
            // Create search point using GeometryFactory
            Point searchPoint = geometryFactory.createPoint(
                new Coordinate(
                    request.getLongitude().doubleValue(),
                    request.getLatitude().doubleValue()
                )
            );
            searchPoint.setSRID(4326);

            int offset = (request.getPage() - 1) * request.getPerPage();
            List<CarPark> carParks =
                carParkMySqlRepository.findNearestCarParksWithPoint(
                    searchPoint,
                    request.getPerPage(),
                    offset
                );

            return carParks
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding nearest car parks", e);
            throw new RuntimeException("Failed to find nearest car parks", e);
        }
    }

    /**
     * Get all car parks with availability (cached)
     */
    @Cacheable(value = "availableCarParks")
    public List<CarParkResponseDTO> getAllCarParksWithAvailability() {
        logger.info("Cache miss - fetching all available car parks");

        try {
            List<CarPark> carParks =
                carParkMySqlRepository.findCarParksWithAvailability();

            return carParks
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching all car parks with availability", e);
            throw new RuntimeException(
                "Failed to fetch car parks with availability",
                e
            );
        }
    }

    /**
     * Find nearest car parks with limit (cached) using spatial functions
     */
    @Cacheable(
        value = "nearestCarParksLimit",
        key = "#latitude + '_' + #longitude + '_' + #limit"
    )
    public List<CarParkResponseDTO> findNearestCarParksWithLimit(
        BigDecimal latitude,
        BigDecimal longitude,
        int limit
    ) {
        logger.info(
            "Cache miss - fetching nearest car parks with limit for lat: {}, lon: {}, limit: {}",
            latitude,
            longitude,
            limit
        );

        try {
            // Create search point using GeometryFactory
            Point searchPoint = geometryFactory.createPoint(
                new Coordinate(longitude.doubleValue(), latitude.doubleValue())
            );
            searchPoint.setSRID(4326);

            List<CarPark> carParks =
                carParkMySqlRepository.findNearestCarParksWithPoint(
                    searchPoint,
                    limit,
                    0
                );

            return carParks
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding nearest car parks with limit", e);
            throw new RuntimeException(
                "Failed to find nearest car parks with limit",
                e
            );
        }
    }

    /**
     * Convert CarPark entity to CarParkResponseDTO
     */
    private CarParkResponseDTO convertToResponseDTO(CarPark carPark) {
        return new CarParkResponseDTO(
            carPark.getAddress(),
            carPark.getLatitude(),
            carPark.getLongitude(),
            carPark.getTotalLots(),
            carPark.getAvailableLots()
        );
    }
}
