package com.example.carpark.service;

import com.example.carpark.entity.CarPark;
import com.example.carpark.repository.mysql.CarParkMySqlRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CarParkManagementService {

    private static final Logger logger = LoggerFactory.getLogger(
        CarParkManagementService.class
    );

    private final CarParkMySqlRepository carParkMySqlRepository;

    public CarParkManagementService(
        CarParkMySqlRepository carParkMySqlRepository
    ) {
        this.carParkMySqlRepository = carParkMySqlRepository;
    }

    /**
     * Soft delete a car park
     */
    public boolean softDeleteCarPark(String carParkNo, String deletedBy) {
        try {
            Optional<CarPark> carParkOpt =
                carParkMySqlRepository.findByCarParkNoAndDeletedAtIsNull(
                    carParkNo
                );

            if (carParkOpt.isPresent()) {
                CarPark carPark = carParkOpt.get();
                carPark.softDelete();
                carPark.setUpdatedBy(deletedBy);
                carParkMySqlRepository.save(carPark);

                logger.info(
                    "Soft deleted car park: {} by user: {}",
                    carParkNo,
                    deletedBy
                );
                return true;
            } else {
                logger.warn(
                    "Car park not found for soft delete: {}",
                    carParkNo
                );
                return false;
            }
        } catch (Exception e) {
            logger.error("Error soft deleting car park: {}", carParkNo, e);
            return false;
        }
    }

    /**
     * Restore a soft-deleted car park
     */
    public boolean restoreCarPark(String carParkNo, String restoredBy) {
        try {
            Optional<CarPark> carParkOpt =
                carParkMySqlRepository.findByCarParkNo(carParkNo);

            if (carParkOpt.isPresent()) {
                CarPark carPark = carParkOpt.get();

                if (carPark.isDeleted()) {
                    carPark.setDeletedAt(null);
                    carPark.setUpdatedBy(restoredBy);
                    carParkMySqlRepository.save(carPark);

                    logger.info(
                        "Restored car park: {} by user: {}",
                        carParkNo,
                        restoredBy
                    );
                    return true;
                } else {
                    logger.warn("Car park is not deleted: {}", carParkNo);
                    return false;
                }
            } else {
                logger.warn("Car park not found for restore: {}", carParkNo);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error restoring car park: {}", carParkNo, e);
            return false;
        }
    }

    /**
     * Get car park by number (including deleted)
     */
    public Optional<CarPark> getCarParkByNumber(String carParkNo) {
        return carParkMySqlRepository.findByCarParkNo(carParkNo);
    }

    /**
     * Get car park by number (non-deleted only)
     */
    public Optional<CarPark> getActiveCarParkByNumber(String carParkNo) {
        return carParkMySqlRepository.findByCarParkNoAndDeletedAtIsNull(
            carParkNo
        );
    }

    /**
     * Get all car parks (non-deleted only)
     */
    public List<CarPark> getAllActiveCarParks() {
        return carParkMySqlRepository.findAllActive();
    }

    /**
     * Get car parks by type (non-deleted only)
     */
    public List<CarPark> getCarParksByType(String carParkType) {
        return carParkMySqlRepository.findByCarParkType(carParkType);
    }

    /**
     * Get car parks by parking system type (non-deleted only)
     */
    public List<CarPark> getCarParksByParkingSystem(String parkingSystemType) {
        return carParkMySqlRepository.findByShortTermParking(parkingSystemType);
    }

    /**
     * Count available car parks (non-deleted only)
     */
    public long countAvailableCarParks() {
        return carParkMySqlRepository.countCarParksWithAvailability();
    }

    /**
     * Get car parks created after a specific date (non-deleted only)
     */
    public List<CarPark> getCarParksCreatedAfter(LocalDateTime date) {
        // TODO: Add this method to CarParkMySqlRepository if needed
        logger.warn(
            "getCarParksCreatedAfter method not implemented in new repository"
        );
        return new ArrayList<>();
    }

    /**
     * Get car parks updated after a specific date (non-deleted only)
     */
    public List<CarPark> getCarParksUpdatedAfter(LocalDateTime date) {
        // TODO: Add this method to CarParkMySqlRepository if needed
        logger.warn(
            "getCarParksUpdatedAfter method not implemented in new repository"
        );
        return new ArrayList<>();
    }

    /**
     * Update car park availability manually
     */
    public boolean updateCarParkAvailability(
        String carParkNo,
        Integer totalLots,
        Integer availableLots,
        String updatedBy
    ) {
        try {
            Optional<CarPark> carParkOpt =
                carParkMySqlRepository.findByCarParkNoAndDeletedAtIsNull(
                    carParkNo
                );

            if (carParkOpt.isPresent()) {
                CarPark carPark = carParkOpt.get();
                carPark.setTotalLots(totalLots);
                carPark.setAvailableLots(availableLots);
                carPark.setUpdatedBy(updatedBy);
                carParkMySqlRepository.save(carPark);

                logger.info(
                    "Updated car park availability: {} - Total: {}, Available: {}",
                    carParkNo,
                    totalLots,
                    availableLots
                );
                return true;
            } else {
                logger.warn(
                    "Car park not found for availability update: {}",
                    carParkNo
                );
                return false;
            }
        } catch (Exception e) {
            logger.error(
                "Error updating car park availability: {}",
                carParkNo,
                e
            );
            return false;
        }
    }
}
