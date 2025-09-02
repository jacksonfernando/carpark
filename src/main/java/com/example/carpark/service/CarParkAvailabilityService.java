package com.example.carpark.service;

import com.example.carpark.entity.CarPark;
import com.example.carpark.entity.CarParkAvailability;
import com.example.carpark.repository.external.CarParkExternalApiRepository;
import com.example.carpark.repository.mysql.CarParkMySqlRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CarParkAvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(
        CarParkAvailabilityService.class
    );

    private final CarParkMySqlRepository carParkMySqlRepository;
    private final CarParkExternalApiRepository carParkExternalApiRepository;
    private final GeometryFactory geometryFactory;

    public CarParkAvailabilityService(
        CarParkMySqlRepository carParkMySqlRepository,
        CarParkExternalApiRepository carParkExternalApiRepository,
        GeometryFactory geometryFactory
    ) {
        this.carParkMySqlRepository = carParkMySqlRepository;
        this.carParkExternalApiRepository = carParkExternalApiRepository;
        this.geometryFactory = geometryFactory;
    }

    /**
     * Update car park availability data from the Singapore government API using streaming
     */
    public void updateCarParkAvailability() {
        logger.info("Starting car park availability update with streaming");

        try {
            // Use external API repository to fetch data with streaming
            carParkExternalApiRepository.fetchCarParkAvailabilityStreaming(
                this::processCarParkAvailabilityData
            );
        } catch (Exception e) {
            logger.error("Error updating car park availability", e);
            throw new RuntimeException(
                "Failed to update car park availability",
                e
            );
        }
    }

    /**
     * Process car park availability data received from external API
     * Implements upsert: update if exists, insert if not exists
     */
    private void processCarParkAvailabilityData(CarParkAvailability data) {
        try {
            logger.debug(
                "Processing car park availability data: {} - Total: {}, Available: {}, Type: {}",
                data.getCarparkNumber(),
                data.getTotalLots(),
                data.getAvailableLots(),
                data.getLotType()
            );

            // Try to update existing car park
            int updatedRows =
                carParkMySqlRepository.updateCarParkAvailabilityBatch(
                    data.getCarparkNumber(),
                    data.getTotalLots(),
                    data.getAvailableLots(),
                    data.getLotType(),
                    "SYSTEM"
                );

            if (updatedRows > 0) {
                logger.debug(
                    "Updated existing car park availability: {}",
                    data.getCarparkNumber()
                );
            } else {
                // Car park doesn't exist, create a new one with basic information
                logger.info(
                    "Car park {} not found, creating new entry",
                    data.getCarparkNumber()
                );
                createNewCarParkFromAvailabilityData(data);
            }
        } catch (Exception e) {
            logger.error(
                "Error processing car park {}: {}",
                data.getCarparkNumber(),
                e.getMessage()
            );
        }
    }

    /**
     * Create a new car park entry from availability data
     */
    private void createNewCarParkFromAvailabilityData(CarParkAvailability data) {
        try {
            // Create a basic car park entry with the data we have
            CarPark newCarPark = new CarPark(
                data.getCarparkNumber(),
                "Car Park " + data.getCarparkNumber(), // Basic address
                new BigDecimal("1.3521"), // Default Singapore latitude
                new BigDecimal("103.8198"), // Default Singapore longitude
                data.getTotalLots(),
                data.getAvailableLots(),
                "MULTI-STOREY CAR PARK", // Default type
                "ELECTRONIC PARKING", // Default parking system
                "WHOLE DAY", // Default short term parking
                "NO", // Default free parking
                "YES", // Default night parking
                "0", // Default car park decks
                "0", // Default gantry height
                "N" // Default car park basement
            );

            // Create spatial location point
            Point location = geometryFactory.createPoint(
                new Coordinate(
                    new BigDecimal("103.8198").doubleValue(),
                    new BigDecimal("1.3521").doubleValue()
                )
            );
            location.setSRID(4326);
            newCarPark.setLocation(location);

            newCarPark.setCreatedBy("SYSTEM");
            newCarPark.setUpdatedBy("SYSTEM");

            carParkMySqlRepository.save(newCarPark);
            logger.info("Created new car park: {}", data.getCarparkNumber());
        } catch (Exception e) {
            logger.error(
                "Error creating new car park {}: {}",
                data.getCarparkNumber(),
                e.getMessage()
            );
        }
    }

    /**
     * Scheduled task to update car park availability every 15 minutes
     */
    @Scheduled(fixedRate = 900000) // 15 minutes
    public void scheduledAvailabilityUpdate() {
        logger.info(
            "🕐 Scheduled car park availability update triggered at {}",
            LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            )
        );
        try {
            updateCarParkAvailability();
            logger.info(
                "✅ Scheduled car park availability update completed successfully"
            );
        } catch (Exception e) {
            logger.error("❌ Scheduled car park availability update failed", e);
        }
    }
}
