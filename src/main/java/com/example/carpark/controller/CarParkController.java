package com.example.carpark.controller;

import com.example.carpark.common.constants.CarParkConstants;
import com.example.carpark.common.exception.CarParkException;
import com.example.carpark.common.exception.ErrorCode;
import com.example.carpark.dto.request.NearestCarParkRequestDTO;
import com.example.carpark.dto.response.CarParkResponseDTO;
import com.example.carpark.service.CachedCarParkService;
import com.example.carpark.service.CarParkAvailabilityService;
import com.example.carpark.service.CarParkStreamingImportService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(CarParkConstants.API_BASE_PATH)
@CrossOrigin(origins = "*")
public class CarParkController {

    private static final Logger logger = LoggerFactory.getLogger(
        CarParkController.class
    );

    private final CachedCarParkService cachedCarParkService;
    private final CarParkStreamingImportService carParkStreamingImportService;
    private final CarParkAvailabilityService carParkAvailabilityService;

    public CarParkController(
        CachedCarParkService cachedCarParkService,
        CarParkStreamingImportService carParkStreamingImportService,
        CarParkAvailabilityService carParkAvailabilityService
    ) {
        this.cachedCarParkService = cachedCarParkService;
        this.carParkStreamingImportService = carParkStreamingImportService;
        this.carParkAvailabilityService = carParkAvailabilityService;
    }

    /**
     * Find nearest car parks to a given location
     */
    @GetMapping(CarParkConstants.NEAREST_ENDPOINT)
    public ResponseEntity<List<CarParkResponseDTO>> findNearestCarParks(
        @Valid NearestCarParkRequestDTO request
    ) {
        try {
            List<CarParkResponseDTO> carParks =
                cachedCarParkService.findNearestCarParks(request);
            return ResponseEntity.ok(carParks);
        } catch (Exception e) {
            logger.error("Error finding nearest car parks", e);
            throw new CarParkException(
                "Error finding nearest car parks",
                ErrorCode.DATABASE_OPERATION_FAILED.getCode(),
                e
            );
        }
    }

    /**
     * Get all car parks with availability
     */
    @GetMapping(CarParkConstants.AVAILABLE_ENDPOINT)
    public ResponseEntity<
        List<CarParkResponseDTO>
    > getAllCarParksWithAvailability() {
        try {
            List<CarParkResponseDTO> carParks =
                cachedCarParkService.getAllCarParksWithAvailability();
            return ResponseEntity.ok(carParks);
        } catch (Exception e) {
            logger.error("Error fetching car parks with availability", e);
            throw new CarParkException(
                "Error fetching car parks with availability",
                ErrorCode.DATABASE_OPERATION_FAILED.getCode(),
                e
            );
        }
    }

    /**
     * Import car park data from CSV using streaming
     */
    @PostMapping(CarParkConstants.IMPORT_ENDPOINT)
    public ResponseEntity<String> importCarParkData() {
        try {
            carParkStreamingImportService.importCarParkDataStreaming();
            return ResponseEntity.ok(CarParkConstants.SUCCESS_IMPORT_COMPLETED);
        } catch (Exception e) {
            logger.error("Error importing car park data", e);
            throw new CarParkException(
                CarParkConstants.ERROR_CSV_IMPORT_FAILED,
                ErrorCode.CSV_IMPORT_FAILED.getCode(),
                e
            );
        }
    }

    /**
     * Update car park availability from external API
     */
    @PostMapping(CarParkConstants.UPDATE_AVAILABILITY_ENDPOINT)
    public ResponseEntity<String> updateCarParkAvailability() {
        try {
            logger.info(
                "Starting car park availability update from external API..."
            );
            carParkAvailabilityService.updateCarParkAvailability();
            return ResponseEntity.ok(
                CarParkConstants.SUCCESS_AVAILABILITY_UPDATED
            );
        } catch (Exception e) {
            logger.error("Error updating car park availability", e);
            throw new CarParkException(
                CarParkConstants.ERROR_AVAILABILITY_UPDATE_FAILED,
                ErrorCode.AVAILABILITY_UPDATE_FAILED.getCode(),
                e
            );
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping(CarParkConstants.HEALTH_ENDPOINT)
    public ResponseEntity<String> health() {
        return ResponseEntity.ok(CarParkConstants.SUCCESS_HEALTH_CHECK);
    }
}
