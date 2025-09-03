package com.example.carpark.service;

import com.example.carpark.entity.CarPark;
import com.example.carpark.repository.mysql.CarParkMySqlRepository;
import com.example.carpark.service.CoordinateConversionService;
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

    private static final Logger logger = LoggerFactory.getLogger(CarParkStreamingImportService.class);
    private static final int CHUNK_SIZE = 100;

    @Value("${carpark.data.csv.path}")
    private String csvFilePath;

    private final CarParkMySqlRepository carParkMySqlRepository;
    private final RedisGeospatialService redisGeospatialService;
    private final GeometryFactory geometryFactory;
    private final CoordinateConversionService coordinateConversionService;

    public CarParkStreamingImportService(
            CarParkMySqlRepository carParkMySqlRepository,
            RedisGeospatialService redisGeospatialService,
            GeometryFactory geometryFactory,
            CoordinateConversionService coordinateConversionService) {
        this.carParkMySqlRepository = carParkMySqlRepository;
        this.redisGeospatialService = redisGeospatialService;
        this.geometryFactory = geometryFactory;
        this.coordinateConversionService = coordinateConversionService;
    }

    /**
     * Import car park data from CSV using streaming approach
     */
    @Transactional
    public void importCarParkDataStreaming() {
        logger.info("Starting streaming import of car park data from: {}", csvFilePath);

        ImportStats stats = new ImportStats();
        List<CarPark> currentChunk = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            String[] header = reader.readNext();
            if (header == null) {
                throw new RuntimeException("CSV file is empty or invalid");
            }
            logger.info("CSV header: {}", String.join(", ", header));

            processCsvRows(reader, stats, currentChunk);
            processFinalChunk(currentChunk, stats);

            logger.info("Streaming import completed. Total processed: {}, Total imported: {}",
                    stats.getTotalProcessed(), stats.getTotalImported());

            cacheCarParkLocationsInRedis();
        } catch (IOException | CsvValidationException e) {
            logger.error("Error reading CSV file", e);
            throw new RuntimeException("Failed to import car park data", e);
        }
    }

    /**
     * Process CSV rows in chunks
     */
    private void processCsvRows(CSVReader reader, ImportStats stats, List<CarPark> currentChunk)
            throws IOException, CsvValidationException {
        String[] row;
        while ((row = reader.readNext()) != null) {
            try {
                CarPark carPark = parseCarParkRow(row);
                if (carPark != null) {
                    currentChunk.add(carPark);
                    stats.incrementProcessed();

                    if (currentChunk.size() >= CHUNK_SIZE) {
                        int chunkImported = processChunk(currentChunk);
                        stats.addImported(chunkImported);
                        currentChunk.clear();

                        logger.info("Processed chunk: {} items, {} imported. Total so far: {} processed, {} imported",
                                CHUNK_SIZE, chunkImported, stats.getTotalProcessed(), stats.getTotalImported());
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing row {}: {}", stats.getTotalProcessed() + 1, e.getMessage());
            }
        }
    }

    /**
     * Process the final chunk of car parks
     */
    private void processFinalChunk(List<CarPark> currentChunk, ImportStats stats) {
        if (!currentChunk.isEmpty()) {
            int chunkImported = processChunk(currentChunk);
            stats.addImported(chunkImported);
            logger.info("Final chunk: {} items, {} imported", currentChunk.size(), chunkImported);
        }
    }

    /**
     * Cache car park locations in Redis after import
     */
    private void cacheCarParkLocationsInRedis() {
        try {
            logger.info("Caching car park locations in Redis...");
            List<CarPark> allCarParks = carParkMySqlRepository.findAll();
            redisGeospatialService.cacheCarParkLocations(allCarParks);
            logger.info("Successfully cached {} car park locations in Redis", allCarParks.size());
        } catch (Exception e) {
            logger.error("Failed to cache car park locations in Redis", e);
        }
    }

    /**
     * Process a chunk of car parks
     */
    private int processChunk(List<CarPark> chunk) {
        int importedCount = 0;

        for (CarPark carPark : chunk) {
            try {
                var existingCarPark = carParkMySqlRepository.findByCarParkNoAndDeletedAtIsNull(
                        carPark.getCarParkNo());

                if (existingCarPark.isPresent()) {
                    updateExistingCarPark(existingCarPark.get(), carPark);
                    logger.debug("Updated existing car park: {}", carPark.getCarParkNo());
                } else {
                    carParkMySqlRepository.save(carPark);
                    logger.debug("Imported new car park: {}", carPark.getCarParkNo());
                }

                importedCount++;
            } catch (Exception e) {
                logger.error("Error saving car park {}: {}", carPark.getCarParkNo(), e.getMessage());
            }
        }

        return importedCount;
    }

    /**
     * Update existing car park with new data
     */
    private void updateExistingCarPark(CarPark existing, CarPark newData) {
        existing.setAddress(newData.getAddress());
        existing.setLatitude(newData.getLatitude());
        existing.setLongitude(newData.getLongitude());
        existing.setLocation(createSpatialLocation(newData.getLongitude(), newData.getLatitude()));
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

        carParkMySqlRepository.save(existing);
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

            BigDecimal[] wgs84Coords = coordinateConversionService.convertSVY21ToWGS84(
                    new BigDecimal(row[2].trim()), new BigDecimal(row[3].trim()));

            logger.debug("Processing car park: {} with coordinates: {}, {}",
                    carParkNo, wgs84Coords[0], wgs84Coords[1]);

            CarPark carPark = createCarParkFromRow(row, carParkNo, wgs84Coords);
            carPark.setLocation(createSpatialLocation(wgs84Coords[1], wgs84Coords[0]));
            carPark.setCreatedBy("SYSTEM");
            carPark.setUpdatedBy("SYSTEM");

            return carPark;
        } catch (Exception e) {
            logger.error("Error parsing car park row: {}", String.join(", ", row), e);
            return null;
        }
    }

    /**
     * Create CarPark entity from CSV row data
     */
    private CarPark createCarParkFromRow(String[] row, String carParkNo, BigDecimal[] wgs84Coords) {
        return new CarPark(
                carParkNo,
                row[1].trim(), // address
                wgs84Coords[0], // latitude
                wgs84Coords[1], // longitude
                0, // total_lots - not in CSV, default to 0
                0, // available_lots - not in CSV, default to 0
                row[4].trim(), // car_park_type
                row[5].trim(), // type_of_parking_system
                row[6].trim(), // short_term_parking
                row[7].trim(), // free_parking
                row[8].trim(), // night_parking
                row[9].trim(), // car_park_decks
                row[10].trim(), // gantry_height
                row[11].trim() // car_park_basement
        );
    }

    /**
     * Create spatial location point
     */
    private Point createSpatialLocation(BigDecimal longitude, BigDecimal latitude) {
        try {
            Point location = geometryFactory.createPoint(
                    new Coordinate(longitude.doubleValue(), latitude.doubleValue()));
            location.setSRID(4326);
            return location;
        } catch (Exception e) {
            logger.error("Error creating location point for coordinates: {}, {}", longitude, latitude, e);
            throw e;
        }
    }

    /**
     * Get import statistics
     */
    public ImportStats getImportStats() {
        long totalCarParks = carParkMySqlRepository.countActiveCarParks();
        long availableCarParks = carParkMySqlRepository.countCarParksWithAvailability();
        return new ImportStats(totalCarParks, availableCarParks);
    }

    /**
     * Import statistics data class
     */
    public static class ImportStats {
        private long totalProcessed = 0;
        private long totalImported = 0;

        public ImportStats() {
        }

        public ImportStats(long totalCarParks, long availableCarParks) {
            this.totalProcessed = totalCarParks;
            this.totalImported = availableCarParks;
        }

        public void incrementProcessed() {
            this.totalProcessed++;
        }

        public void addImported(int count) {
            this.totalImported += count;
        }

        public long getTotalProcessed() {
            return totalProcessed;
        }

        public long getTotalImported() {
            return totalImported;
        }
    }
}
