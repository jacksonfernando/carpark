package com.example.carpark.service;

import com.example.carpark.entity.CarPark;
import com.example.carpark.repository.mysql.CarParkMySqlRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CarParkDataImportService {

    private static final Logger logger = LoggerFactory.getLogger(
        CarParkDataImportService.class
    );

    @Value("${carpark.data.csv.path}")
    private String csvFilePath;

    private final CarParkMySqlRepository carParkMySqlRepository;
    private final CoordinateConversionService coordinateConversionService;

    public CarParkDataImportService(
        CarParkMySqlRepository carParkMySqlRepository,
        CoordinateConversionService coordinateConversionService
    ) {
        this.carParkMySqlRepository = carParkMySqlRepository;
        this.coordinateConversionService = coordinateConversionService;
    }

    /**
     * Import car park data from CSV file
     */
    public void importCarParkData() {
        logger.info("Starting car park data import from: {}", csvFilePath);

        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath))) {
            String[] header = reader.readNext(); // Skip header row
            if (header == null) {
                throw new RuntimeException("CSV file is empty or invalid");
            }

            List<CarPark> carParks = new ArrayList<>();
            String[] row;
            int processedCount = 0;
            int updatedCount = 0;
            int createdCount = 0;

            while ((row = reader.readNext()) != null) {
                try {
                    CarPark carPark = parseCarParkRow(row);
                    if (carPark != null) {
                        // Check if car park already exists
                        var existingCarParkOpt =
                            carParkMySqlRepository.findByCarParkNoAndDeletedAtIsNull(
                                carPark.getCarParkNo()
                            );

                        if (existingCarParkOpt.isPresent()) {
                            // Update existing car park
                            CarPark existingCarPark = existingCarParkOpt.get();
                            updateCarParkFields(existingCarPark, carPark);
                            carParkMySqlRepository.save(existingCarPark);
                            updatedCount++;
                        } else {
                            // Create new car park
                            carPark.setCreatedBy("CSV_IMPORT");
                            carParks.add(carPark);
                            createdCount++;
                        }

                        processedCount++;

                        // Batch save every 100 records
                        if (carParks.size() >= 100) {
                            carParkMySqlRepository.saveAll(carParks);
                            carParks.clear();
                            logger.info(
                                "Processed {} car parks ({} created, {} updated)",
                                processedCount,
                                createdCount,
                                updatedCount
                            );
                        }
                    }
                } catch (Exception e) {
                    logger.error(
                        "Error processing row: {}",
                        String.join(",", row),
                        e
                    );
                }
            }

            // Save remaining records
            if (!carParks.isEmpty()) {
                carParkMySqlRepository.saveAll(carParks);
            }

            logger.info(
                "Car park data import completed. Total processed: {}, Created: {}, Updated: {}",
                processedCount,
                createdCount,
                updatedCount
            );
        } catch (IOException | CsvValidationException e) {
            logger.error("Error reading CSV file", e);
            throw new RuntimeException("Failed to import car park data", e);
        }
    }

    /**
     * Update existing car park fields with new data
     */
    private void updateCarParkFields(
        CarPark existingCarPark,
        CarPark newCarPark
    ) {
        existingCarPark.setAddress(newCarPark.getAddress());
        existingCarPark.setLatitude(newCarPark.getLatitude());
        existingCarPark.setLongitude(newCarPark.getLongitude());
        existingCarPark.setCarParkType(newCarPark.getCarParkType());
        existingCarPark.setTypeOfParkingSystem(
            newCarPark.getTypeOfParkingSystem()
        );
        existingCarPark.setShortTermParking(newCarPark.getShortTermParking());
        existingCarPark.setFreeParking(newCarPark.getFreeParking());
        existingCarPark.setNightParking(newCarPark.getNightParking());
        existingCarPark.setCarParkDecks(newCarPark.getCarParkDecks());
        existingCarPark.setGantryHeight(newCarPark.getGantryHeight());
        existingCarPark.setCarParkBasement(newCarPark.getCarParkBasement());
        existingCarPark.setUpdatedBy("CSV_IMPORT");
    }

    /**
     * Parse a single row from the CSV file
     * Expected CSV columns: car_park_no,address,x_coord,y_coord,car_park_type,type_of_parking_system,short_term_parking,free_parking,night_parking,car_park_decks,gantry_height,car_park_basement
     */
    private CarPark parseCarParkRow(String[] row) {
        try {
            String carParkNo = row[0];
            String address = row[1];
            double xCoord = Double.parseDouble(row[2]);
            double yCoord = Double.parseDouble(row[3]);
            String carParkType = row[4];
            String typeOfParkingSystem = row[5];
            String shortTermParking = row[6];
            String freeParking = row[7];
            String nightParking = row[8];
            String carParkDecks = row[9];
            String gantryHeight = row[10];
            String carParkBasement = row[11];

            // Convert SVY21 coordinates to WGS84
            BigDecimal[] coordinates =
                coordinateConversionService.convertSVY21ToWGS84(xCoord, yCoord);
            BigDecimal latitude = coordinates[0];
            BigDecimal longitude = coordinates[1];

            CarPark carPark = new CarPark(
                carParkNo,
                address,
                latitude,
                longitude
            );
            carPark.setCarParkType(carParkType);
            carPark.setTypeOfParkingSystem(typeOfParkingSystem);
            carPark.setShortTermParking(shortTermParking);
            carPark.setFreeParking(freeParking);
            carPark.setNightParking(nightParking);
            carPark.setCarParkDecks(carParkDecks);
            carPark.setGantryHeight(gantryHeight);
            carPark.setCarParkBasement(carParkBasement);

            return carPark;
        } catch (Exception e) {
            logger.error(
                "Error parsing car park row: {}",
                String.join(",", row),
                e
            );
            return null;
        }
    }
}
