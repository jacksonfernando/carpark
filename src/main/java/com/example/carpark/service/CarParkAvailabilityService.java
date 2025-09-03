package com.example.carpark.service;

import com.example.carpark.entity.CarParkAvailability;
import com.example.carpark.repository.external.CarParkExternalApiRepository;
import com.example.carpark.repository.mysql.CarParkMySqlRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CarParkAvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(CarParkAvailabilityService.class);

    private final CarParkMySqlRepository carParkMySqlRepository;
    private final CarParkExternalApiRepository carParkExternalApiRepository;
    private final RedisGeospatialService redisGeospatialService;

    public CarParkAvailabilityService(
            CarParkMySqlRepository carParkMySqlRepository,
            CarParkExternalApiRepository carParkExternalApiRepository,
            RedisGeospatialService redisGeospatialService) {
        this.carParkMySqlRepository = carParkMySqlRepository;
        this.carParkExternalApiRepository = carParkExternalApiRepository;
        this.redisGeospatialService = redisGeospatialService;
    }

    /**
     * Update car park availability data from the Singapore government API using
     * streaming
     */
    public void updateCarParkAvailability() {
        logger.info("Starting car park availability update with streaming");

        try {
            carParkExternalApiRepository.fetchCarParkAvailabilityStreaming(
                    this::processCarParkAvailabilityData);
        } catch (Exception e) {
            logger.error("Error updating car park availability", e);
            throw new RuntimeException("Failed to update car park availability", e);
        }
    }

    /**
     * Process car park availability data received from external API
     * Only updates existing car parks - does not create new ones without
     * coordinates
     */
    private void processCarParkAvailabilityData(CarParkAvailability data) {
        try {
            logger.debug("Processing car park: {} - Total: {}, Available: {}, Type: {}",
                    data.getCarparkNumber(), data.getTotalLots(), data.getAvailableLots(), data.getLotType());

            int updatedRows = carParkMySqlRepository.updateCarParkAvailabilityBatch(
                    data.getCarparkNumber(), data.getTotalLots(), data.getAvailableLots(),
                    data.getLotType(), "SYSTEM");

            if (updatedRows > 0) {
                logger.debug("Updated existing car park: {}", data.getCarparkNumber());
            } else {
                // Check if car park exists but couldn't be updated (maybe different schema)
                var existingCarPark = carParkMySqlRepository.findByCarParkNoAndDeletedAtIsNull(data.getCarparkNumber());

                if (existingCarPark.isPresent()) {
                    // Car park exists but update failed - log warning and skip
                    logger.warn("Car park {} exists but update failed - skipping", data.getCarparkNumber());
                } else {
                    // Car park doesn't exist - we can't create it without coordinates
                    // The external API only provides availability data, not location data
                    logger.info("Car park {} not found in database. Cannot create new car park without coordinates. " +
                            "Please import car park data from CSV first.", data.getCarparkNumber());
                }
            }
        } catch (Exception e) {
            logger.error("Error processing car park {}: {}", data.getCarparkNumber(), e.getMessage());
        }
    }

    /**
     * Scheduled task to update car park availability every 15 minutes
     */
    @Scheduled(fixedRate = 900000) // 15 minutes
    public void scheduledAvailabilityUpdate() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logger.info("🕐 Scheduled car park availability update triggered at {}", timestamp);

        try {
            updateCarParkAvailability();
            logger.info("🔄 Refreshing location cache to sync with availability data...");
            redisGeospatialService.refreshLocationCache();
            logger.info("✅ Scheduled car park availability update completed successfully");
        } catch (Exception e) {
            logger.error("❌ Scheduled car park availability update failed", e);
        }
    }

    /**
     * Update multiple car parks in a single database operation using CASE
     * statements
     */
    private int updateCarParksInBatch(List<CarParkAvailability> carParksToUpdate) {
        if (carParksToUpdate.isEmpty()) {
            return 0;
        }

        BatchUpdateBuilder builder = new BatchUpdateBuilder();
        List<String> carParkNumbers = new ArrayList<>();

        for (CarParkAvailability data : carParksToUpdate) {
            String carparkNo = data.getCarparkNumber();
            carParkNumbers.add(carparkNo);
            builder.addCarParkData(carparkNo, data);
        }

        logger.debug("Executing batch update for {} car parks", carParksToUpdate.size());

        return carParkMySqlRepository.updateCarParkAvailabilityBatchMultiple(
                builder.getTotalLotsCases(), builder.getAvailableLotsCases(),
                builder.getCarParkTypeCases(), carParkNumbers, "SYSTEM");
    }

    /**
     * Helper class for building batch update CASE statements
     */
    private static class BatchUpdateBuilder {
        private final StringBuilder totalLotsCases = new StringBuilder();
        private final StringBuilder availableLotsCases = new StringBuilder();
        private final StringBuilder carParkTypeCases = new StringBuilder();

        public void addCarParkData(String carparkNo, CarParkAvailability data) {
            totalLotsCases.append("WHEN '").append(carparkNo).append("' THEN ").append(data.getTotalLots());
            availableLotsCases.append("WHEN '").append(carparkNo).append("' THEN ").append(data.getAvailableLots());
            carParkTypeCases.append("WHEN '").append(carparkNo).append("' THEN '")
                    .append(data.getLotType() != null ? data.getLotType() : "UNKNOWN").append("'");
        }

        public String getTotalLotsCases() {
            return totalLotsCases.toString() + " ELSE total_lots";
        }

        public String getAvailableLotsCases() {
            return availableLotsCases.toString() + " ELSE available_lots";
        }

        public String getCarParkTypeCases() {
            return carParkTypeCases.toString() + " ELSE car_park_type";
        }
    }

    /**
     * Result holder for batch processing
     */
    private static class BatchResult {
        int updatedCount = 0;
        int createdCount = 0;
        int errorCount = 0;
    }
}
