package com.example.carpark.repository.mysql;

import com.example.carpark.entity.CarPark;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * MySQL repository for car park database operations using Hibernate
 */
@Repository
public interface CarParkMySqlRepository extends JpaRepository<CarPark, Long> {
    /**
     * Find car park by car park number (excluding soft deleted)
     */
    Optional<CarPark> findByCarParkNoAndDeletedAtIsNull(String carParkNo);

    /**
     * Find car park by car park number (including soft deleted)
     */
    Optional<CarPark> findByCarParkNo(String carParkNo);

    /**
     * Find all active car parks (excluding soft deleted)
     */
    @Query("SELECT cp FROM CarPark cp WHERE cp.deletedAt IS NULL")
    List<CarPark> findAllActive();

    /**
     * Find car parks by car park numbers (excluding soft deleted) for batch
     * processing
     */
    @Query("SELECT cp FROM CarPark cp WHERE cp.carParkNo IN :carParkNumbers AND cp.deletedAt IS NULL")
    List<CarPark> findByCarParkNoInAndDeletedAtIsNull(@Param("carParkNumbers") List<String> carParkNumbers);

    /**
     * Find car parks with available lots (excluding soft deleted)
     */
    @Query("SELECT cp FROM CarPark cp WHERE cp.availableLots > 0 AND cp.deletedAt IS NULL")
    List<CarPark> findCarParksWithAvailability();

    /**
     * Find nearest car parks using MySQL spatial functions
     * Returns car parks with available lots, sorted by distance
     */
    @Query(value = """
            SELECT cp.*,
                   ST_Distance_Sphere(:searchPoint, cp.location) / 1000 AS distance_km
            FROM car_parks cp
            WHERE cp.deleted_at IS NULL
              AND cp.available_lots > 0
            ORDER BY distance_km
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<CarPark> findNearestCarParksWithPoint(
            @Param("searchPoint") Point searchPoint,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /**
     * Batch update car park availability using native SQL for better performance
     */
    @Query(value = """
            UPDATE car_parks
            SET total_lots = :totalLots,
                available_lots = :availableLots,
                car_park_type = :carParkType,
                updated_at = NOW(),
                updated_by = :updatedBy
            WHERE car_park_no = :carParkNo
            AND deleted_at IS NULL
            """, nativeQuery = true)
    @Modifying
    @Transactional
    int updateCarParkAvailabilityBatch(
            @Param("carParkNo") String carParkNo,
            @Param("totalLots") Integer totalLots,
            @Param("availableLots") Integer availableLots,
            @Param("carParkType") String carParkType,
            @Param("updatedBy") String updatedBy);

    /**
     * Soft delete a car park
     */
    @Query("UPDATE CarPark cp SET cp.deletedAt = NOW(), cp.updatedAt = NOW(), cp.updatedBy = :updatedBy WHERE cp.id = :id")
    @Modifying
    @Transactional
    int softDeleteById(
            @Param("id") Long id,
            @Param("updatedBy") String updatedBy);

    /**
     * Restore a soft deleted car park
     */
    @Query("UPDATE CarPark cp SET cp.deletedAt = NULL, cp.updatedAt = NOW(), cp.updatedBy = :updatedBy WHERE cp.id = :id")
    @Modifying
    @Transactional
    int restoreById(@Param("id") Long id, @Param("updatedBy") String updatedBy);

    /**
     * Find car parks by car park type
     */
    @Query("SELECT cp FROM CarPark cp WHERE cp.carParkType = :carParkType AND cp.deletedAt IS NULL")
    List<CarPark> findByCarParkType(@Param("carParkType") String carParkType);

    /**
     * Find car parks by short term parking
     */
    @Query("SELECT cp FROM CarPark cp WHERE cp.shortTermParking = :shortTermParking AND cp.deletedAt IS NULL")
    List<CarPark> findByShortTermParking(
            @Param("shortTermParking") String shortTermParking);

    /**
     * Count active car parks
     */
    @Query("SELECT COUNT(cp) FROM CarPark cp WHERE cp.deletedAt IS NULL")
    long countActiveCarParks();

    /**
     * Count car parks with availability
     */
    @Query("SELECT COUNT(cp) FROM CarPark cp WHERE cp.availableLots > 0 AND cp.deletedAt IS NULL")
    long countCarParksWithAvailability();

    /**
     * True batch update for multiple car parks using native SQL with CASE
     * statements
     * This method updates multiple car parks in a single database operation
     */
    @Query(value = """
            UPDATE car_parks
            SET total_lots = CASE car_park_no
                :totalLotsCases
            END,
            available_lots = CASE car_park_no
                :availableLotsCases
            END,
            car_park_type = CASE car_park_no
                :carParkTypeCases
            END,
            updated_at = NOW(),
            updated_by = :updatedBy
            WHERE car_park_no IN (:carParkNumbers)
            AND deleted_at IS NULL
            """, nativeQuery = true)
    @Modifying
    @Transactional
    int updateCarParkAvailabilityBatchMultiple(
            @Param("totalLotsCases") String totalLotsCases,
            @Param("availableLotsCases") String availableLotsCases,
            @Param("carParkTypeCases") String carParkTypeCases,
            @Param("carParkNumbers") List<String> carParkNumbers,
            @Param("updatedBy") String updatedBy);
}
