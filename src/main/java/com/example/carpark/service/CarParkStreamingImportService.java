package com.example.carpark.service;

import com.example.carpark.entity.CarPark;
import com.example.carpark.repository.mysql.CarParkMySqlRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for streaming CSV import of car park data
 */
@Service
public class CarParkStreamingImportService {

    private static final Logger logger = LoggerFactory.getLogger(
        CarParkStreamingImportService.class
    );

    @Value("${carpark.data.csv.path}")
    private String csvFilePath;

    private final CarParkMySqlRepository carParkMySqlRepository;
    private final CoordinateConversionService coordinateConversionService;
    private final GeometryFactory geometryFactory;

    public CarParkStreamingImportService(
        CarParkMySqlRepository carParkMySqlRepository,
        CoordinateConversionService coordinateConversionService,
        GeometryFactory geometryFactory
    ) {
        this.carParkMySqlRepository = carParkMySqlRepository;
        this.coordinateConversionService = coordinateConversionService;
        this.geometryFactory = geometryFactory;
    }

    /**
     * Import car park data from CSV using streaming approach
     */
    @Transactional
    public void importCarParkDataStreaming() {
        logger.info(
            "Starting streaming import of car park data from: {}",
            csvFilePath
        );

        int totalProcessed = 0;
        int totalImported = 0;
        int chunkSize = 100; // Process 100 records at a time
        List<CarPark> currentChunk = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            // Skip header row
            String[] header = reader.readNext();
            if (header == null) {
                throw new RuntimeException("CSV file is empty or invalid");
            }
            logger.info("CSV header: {}", String.join(", ", header));

            String[] row;
            while ((row = reader.readNext()) != null) {
                try {
                    CarPark carPark = parseCarParkRow(row);
                    if (carPark != null) {
                        currentChunk.add(carPark);
                        totalProcessed++;

                        // Process chunk when it reaches the size limit
                        if (currentChunk.size() >= chunkSize) {
                            int chunkImported = processChunk(currentChunk);
                            totalImported += chunkImported;
                            currentChunk.clear();

                            logger.info(
                                "Processed chunk: {} items, {} imported. Total so far: {} processed, {} imported",
                                chunkSize,
                                chunkImported,
                                totalProcessed,
                                totalImported
                            );
                        }
                    }
                } catch (Exception e) {
                    logger.error(
                        "Error processing row {}: {}",
                        totalProcessed + 1,
                        e.getMessage()
                    );
                }
            }

            // Process remaining items in the final chunk
            if (!currentChunk.isEmpty()) {
                int chunkImported = processChunk(currentChunk);
                totalImported += chunkImported;

                logger.info(
                    "Final chunk: {} items, {} imported",
                    currentChunk.size(),
                    chunkImported
                );
            }
        } catch (IOException | CsvValidationException e) {
            logger.error("Error reading CSV file", e);
            throw new RuntimeException("Failed to import car park data", e);
        }

        logger.info(
            "Streaming import completed. Total processed: {}, Total imported: {}",
            totalProcessed,
            totalImported
        );
    }

    /**
     * Process a chunk of car parks
     */
    private int processChunk(List<CarPark> chunk) {
        int importedCount = 0;

        for (CarPark carPark : chunk) {
            try {
                // Check if car park already exists
                var existingCarPark =
                    carParkMySqlRepository.findByCarParkNoAndDeletedAtIsNull(
                        carPark.getCarParkNo()
                    );

                if (existingCarPark.isPresent()) {
                    // Update existing car park
                    CarPark existing = existingCarPark.get();
                    updateCarParkFields(existing, carPark);
                    carParkMySqlRepository.save(existing);
                    logger.debug(
                        "Updated existing car park: {}",
                        carPark.getCarParkNo()
                    );
                } else {
                    // Save new car park
                    carParkMySqlRepository.save(carPark);
                    logger.debug(
                        "Imported new car park: {}",
                        carPark.getCarParkNo()
                    );
                }

                importedCount++;
            } catch (Exception e) {
                logger.error(
                    "Error saving car park {}: {}",
                    carPark.getCarParkNo(),
                    e.getMessage()
                );
            }
        }

        return importedCount;
    }

    /**
     * Parse a single CSV row into a CarPark entity
     */
    private CarPark parseCarParkRow(String[] row) {
        try {
            if (row.length < 12) {
                logger.warn("Row has insufficient columns: {}", row.length);
                return null;
            }

            String carParkNo = row[0].trim();
            if (carParkNo.isEmpty()) {
                return null;
            }

            // Parse coordinates (assuming they are in SVY21 format)
            BigDecimal svy21X = new BigDecimal(row[2].trim()); // x_coord is at index 2
            BigDecimal svy21Y = new BigDecimal(row[3].trim()); // y_coord is at index 3

            // Convert SVY21 to WGS84 using direct mathematical transformation
            BigDecimal[] wgs84Coords = convertSVY21ToWGS84Direct(
                svy21X,
                svy21Y
            );
            BigDecimal latitude = wgs84Coords[0];
            BigDecimal longitude = wgs84Coords[1];

            logger.debug(
                "Processing car park: {} with coordinates: {}, {}",
                carParkNo,
                latitude,
                longitude
            );

            // Create car park entity with audit fields
            CarPark carPark = new CarPark(
                carParkNo,
                row[1].trim(), // address is at index 1
                latitude,
                longitude,
                0, // total_lots - not in CSV, default to 0
                0, // available_lots - not in CSV, default to 0
                row[4].trim(), // car_park_type is at index 4
                row[5].trim(), // type_of_parking_system is at index 5
                row[6].trim(), // short_term_parking is at index 6
                row[7].trim(), // free_parking is at index 7
                row[8].trim(), // night_parking is at index 8
                row[9].trim(), // car_park_decks is at index 9
                row[10].trim(), // gantry_height is at index 10
                row[11].trim() // car_park_basement is at index 11
            );

            // Create spatial location point
            try {
                Point location = geometryFactory.createPoint(
                    new Coordinate(
                        longitude.doubleValue(),
                        latitude.doubleValue()
                    )
                );
                location.setSRID(4326);
                carPark.setLocation(location);
                logger.debug(
                    "Created location point for car park {}: {}",
                    carParkNo,
                    location
                );
            } catch (Exception e) {
                logger.error(
                    "Error creating location point for car park {}: {}",
                    carParkNo,
                    e.getMessage()
                );
                throw e;
            }

            // Set audit fields
            carPark.setCreatedBy("SYSTEM");
            carPark.setUpdatedBy("SYSTEM");

            return carPark;
        } catch (Exception e) {
            logger.error(
                "Error parsing car park row: {}",
                String.join(", ", row),
                e
            );
            return null;
        }
    }

    /**
     * Update existing car park fields with new data
     */
    private void updateCarParkFields(CarPark existing, CarPark newData) {
        existing.setAddress(newData.getAddress());
        existing.setLatitude(newData.getLatitude());
        existing.setLongitude(newData.getLongitude());

        // Update spatial location point
        Point location = geometryFactory.createPoint(
            new Coordinate(
                newData.getLongitude().doubleValue(),
                newData.getLatitude().doubleValue()
            )
        );
        location.setSRID(4326);
        existing.setLocation(location);

        existing.setTotalLots(newData.getTotalLots());
        existing.setAvailableLots(newData.getAvailableLots());
        existing.setCarParkType(newData.getCarParkType());
        existing.setTypeOfParkingSystem(newData.getTypeOfParkingSystem());
        existing.setShortTermParking(newData.getShortTermParking());
        existing.setFreeParking(newData.getFreeParking());
        existing.setNightParking(newData.getNightParking());
        existing.setCarParkDecks(newData.getCarParkDecks());
        existing.setGantryHeight(newData.getGantryHeight());
        existing.setCarParkBasement(newData.getCarParkBasement());
        existing.setUpdatedBy("SYSTEM");
        existing.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Convert SVY21 coordinates to WGS84 using direct mathematical transformation
     * This is a simplified conversion that should work for Singapore coordinates
     */
    private BigDecimal[] convertSVY21ToWGS84Direct(
        BigDecimal svy21X,
        BigDecimal svy21Y
    ) {
        try {
            // Simplified SVY21 to WGS84 conversion for Singapore using BigDecimal
            // This is an approximation - for production use, consider using a more accurate transformation
            BigDecimal yOffset = svy21Y.subtract(new BigDecimal("40000.0"));
            BigDecimal xOffset = svy21X.subtract(new BigDecimal("30000.0"));

            BigDecimal lat = new BigDecimal("1.0").add(
                yOffset.divide(
                    new BigDecimal("100000.0"),
                    8,
                    RoundingMode.HALF_UP
                )
            );
            BigDecimal lon = new BigDecimal("103.0").add(
                xOffset.divide(
                    new BigDecimal("100000.0"),
                    8,
                    RoundingMode.HALF_UP
                )
            );

            return new BigDecimal[] { lat, lon };
        } catch (Exception e) {
            logger.error(
                "Error in direct coordinate conversion: X={}, Y={}",
                svy21X,
                svy21Y,
                e
            );
            // Return default Singapore coordinates if conversion fails
            return new BigDecimal[] {
                new BigDecimal("1.3521"),
                new BigDecimal("103.8198"),
            };
        }
    }

    /**
     * Parse string to integer safely
     */
    private Integer parseInteger(String value) {
        try {
            return value != null && !value.trim().isEmpty()
                ? Integer.parseInt(value.trim())
                : 0;
        } catch (NumberFormatException e) {
            logger.warn("Could not parse integer value: {}", value);
            return 0;
        }
    }

    /**
     * Get import statistics
     */
    public ImportStats getImportStats() {
        long totalCarParks = carParkMySqlRepository.countActiveCarParks();
        long availableCarParks =
            carParkMySqlRepository.countCarParksWithAvailability();

        return new ImportStats(totalCarParks, availableCarParks);
    }

    /**
     * Import statistics data class
     */
    public static class ImportStats {

        private final long totalCarParks;
        private final long availableCarParks;

        public ImportStats(long totalCarParks, long availableCarParks) {
            this.totalCarParks = totalCarParks;
            this.availableCarParks = availableCarParks;
        }

        public long getTotalCarParks() {
            return totalCarParks;
        }

        public long getAvailableCarParks() {
            return availableCarParks;
        }
    }

    /**
     * Convert SVY21 coordinates to WGS84 using proper transformation
     * This is a mathematical conversion without API calls
     */
    private BigDecimal[] convertSVY21ToWGS84(
        BigDecimal svy21X,
        BigDecimal svy21Y
    ) {
        // SVY21 to WGS84 transformation parameters for Singapore
        final double A = 6377304.063;
        final double INV_F = 300.8017;
        final double B = A * (1 - 1 / INV_F);
        final double E2 = 2 * (1 / INV_F) - Math.pow(1 / INV_F, 2);
        final double E4 = Math.pow(E2, 2);
        final double E6 = Math.pow(E2, 3);
        final double N = (A - B) / (A + B);
        final double N2 = Math.pow(N, 2);
        final double N3 = Math.pow(N, 3);
        final double N4 = Math.pow(N, 4);

        // Origin coordinates (Singapore)
        final double originLat = 1.3666666666666667; // 1° 22' 00" N
        final double originLon = 103.83333333333333; // 103° 50' 00" E
        final double originN = 38744.572;
        final double originE = 28001.642;

        // Convert to double for calculations
        double x = svy21X.doubleValue();
        double y = svy21Y.doubleValue();

        // Calculate relative coordinates
        double dN = y - originN;
        double dE = x - originE;

        // Calculate latitude
        double lat =
            originLat +
            (dN / A) * (1 + E2 * Math.cos(originLat) * Math.cos(originLat)) +
            (Math.pow(dN, 2) / (2 * A * A)) *
            Math.sin(originLat) *
            Math.cos(originLat) *
            (1 + 2 * E2 * Math.cos(originLat) * Math.cos(originLat)) +
            (Math.pow(dN, 3) / (6 * A * A * A)) *
            Math.cos(originLat) *
            (2 - Math.pow(Math.sin(originLat), 2));

        // Calculate longitude
        double lon =
            originLon +
            (dE / (A * Math.cos(originLat))) *
            (1 - E2 * Math.sin(originLat) * Math.sin(originLat)) +
            (Math.pow(dE, 2) /
                (2 * A * A * Math.cos(originLat) * Math.cos(originLat))) *
            Math.sin(originLat) *
            (1 + 2 * E2 * Math.cos(originLat) * Math.cos(originLat)) +
            (Math.pow(dE, 3) /
                (6 *
                    A *
                    A *
                    A *
                    Math.cos(originLat) *
                    Math.cos(originLat) *
                    Math.cos(originLat))) *
            (2 - Math.pow(Math.sin(originLat), 2));

        return new BigDecimal[] {
            new BigDecimal(lat).setScale(8, RoundingMode.HALF_UP),
            new BigDecimal(lon).setScale(8, RoundingMode.HALF_UP),
        };
    }
}
