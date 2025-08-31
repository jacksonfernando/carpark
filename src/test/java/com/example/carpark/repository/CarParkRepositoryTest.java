package com.example.carpark.repository;

import com.example.carpark.entity.CarPark;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CarParkRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CarParkRepository carParkRepository;

    private CarPark carPark1;
    private CarPark carPark2;
    private CarPark carPark3;

    @BeforeEach
    void setUp() {
        // Create test car parks
        carPark1 = new CarPark("A1", "Test Address 1", new BigDecimal("1.23456789"), new BigDecimal("103.45678901"));
        carPark1.setTotalLots(100);
        carPark1.setAvailableLots(50);
        carPark1.setCarParkType("SURFACE CAR PARK");
        carPark1.setTypeOfParkingSystem("COUPON");
        carPark1.setCreatedBy("TEST_USER");

        carPark2 = new CarPark("A2", "Test Address 2", new BigDecimal("1.23456790"), new BigDecimal("103.45678902"));
        carPark2.setTotalLots(80);
        carPark2.setAvailableLots(30);
        carPark2.setCarParkType("MULTI-STOREY CAR PARK");
        carPark2.setTypeOfParkingSystem("ELECTRONIC PARKING");
        carPark2.setCreatedBy("TEST_USER");

        carPark3 = new CarPark("A3", "Test Address 3", new BigDecimal("1.23456791"), new BigDecimal("103.45678903"));
        carPark3.setTotalLots(120);
        carPark3.setAvailableLots(0); // No available lots
        carPark3.setCarParkType("SURFACE CAR PARK");
        carPark3.setTypeOfParkingSystem("COUPON");
        carPark3.setCreatedBy("TEST_USER");

        // Persist test data
        entityManager.persistAndFlush(carPark1);
        entityManager.persistAndFlush(carPark2);
        entityManager.persistAndFlush(carPark3);
    }

    @Test
    void testFindByCarParkNoAndDeletedAtIsNull_Success() {
        // When
        Optional<CarPark> result = carParkRepository.findByCarParkNoAndDeletedAtIsNull("A1");

        // Then
        assertTrue(result.isPresent());
        assertEquals("A1", result.get().getCarParkNo());
        assertEquals("Test Address 1", result.get().getAddress());
    }

    @Test
    void testFindByCarParkNoAndDeletedAtIsNull_NotFound() {
        // When
        Optional<CarPark> result = carParkRepository.findByCarParkNoAndDeletedAtIsNull("NON_EXISTENT");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByCarParkNoAndDeletedAtIsNull_DeletedCarPark() {
        // Given - Soft delete car park
        carPark1.softDelete();
        entityManager.persistAndFlush(carPark1);

        // When
        Optional<CarPark> result = carParkRepository.findByCarParkNoAndDeletedAtIsNull("A1");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByCarParkNo_Success() {
        // When
        Optional<CarPark> result = carParkRepository.findByCarParkNo("A1");

        // Then
        assertTrue(result.isPresent());
        assertEquals("A1", result.get().getCarParkNo());
    }

    @Test
    void testFindByCarParkNo_DeletedCarPark() {
        // Given - Soft delete car park
        carPark1.softDelete();
        entityManager.persistAndFlush(carPark1);

        // When
        Optional<CarPark> result = carParkRepository.findByCarParkNo("A1");

        // Then
        assertTrue(result.isPresent()); // Should find even deleted car parks
        assertTrue(result.get().isDeleted());
    }

    @Test
    void testFindByDeletedAtIsNull_Success() {
        // When
        List<CarPark> result = carParkRepository.findByDeletedAtIsNull();

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(cp -> !cp.isDeleted()));
    }

    @Test
    void testFindByAvailableLotsGreaterThanAndDeletedAtIsNull_Success() {
        // When
        List<CarPark> result = carParkRepository.findByAvailableLotsGreaterThanAndDeletedAtIsNull(0);

        // Then
        assertEquals(2, result.size()); // Only carPark1 and carPark2 have available lots > 0
        assertTrue(result.stream().allMatch(cp -> cp.getAvailableLots() > 0));
    }

    @Test
    void testFindAllWithAvailableLots_Success() {
        // When
        List<CarPark> result = carParkRepository.findAllWithAvailableLots();

        // Then
        assertEquals(2, result.size()); // Only carPark1 and carPark2 have available lots > 0
        assertTrue(result.stream().allMatch(cp -> cp.getAvailableLots() > 0));
    }

    @Test
    void testFindByCarParkTypeAndDeletedAtIsNull_Success() {
        // When
        List<CarPark> result = carParkRepository.findByCarParkTypeAndDeletedAtIsNull("SURFACE CAR PARK");

        // Then
        assertEquals(2, result.size()); // carPark1 and carPark3 are surface car parks
        assertTrue(result.stream().allMatch(cp -> "SURFACE CAR PARK".equals(cp.getCarParkType())));
    }

    @Test
    void testFindByTypeOfParkingSystemAndDeletedAtIsNull_Success() {
        // When
        List<CarPark> result = carParkRepository.findByTypeOfParkingSystemAndDeletedAtIsNull("COUPON");

        // Then
        assertEquals(2, result.size()); // carPark1 and carPark3 use coupon system
        assertTrue(result.stream().allMatch(cp -> "COUPON".equals(cp.getTypeOfParkingSystem())));
    }

    @Test
    void testCountAvailableCarParks_Success() {
        // When
        long result = carParkRepository.countAvailableCarParks();

        // Then
        assertEquals(2, result); // Only carPark1 and carPark2 have available lots > 0
    }

    @Test
    void testFindByCreatedAtAfterAndDeletedAtIsNull_Success() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        // When
        List<CarPark> result = carParkRepository.findByCreatedAtAfterAndDeletedAtIsNull(yesterday);

        // Then
        assertEquals(3, result.size()); // All car parks were created recently
    }

    @Test
    void testFindByUpdatedAtAfterAndDeletedAtIsNull_Success() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        // When
        List<CarPark> result = carParkRepository.findByUpdatedAtAfterAndDeletedAtIsNull(yesterday);

        // Then
        assertEquals(3, result.size()); // All car parks were updated recently
    }

    @Test
    void testFindNearestCarParksWithDistance_Success() {
        // Given
        BigDecimal latitude = new BigDecimal("1.23456789");
        BigDecimal longitude = new BigDecimal("103.45678901");
        PageRequest pageRequest = PageRequest.of(0, 10);

        // When
        Page<CarPark> result = carParkRepository.findNearestCarParksWithDistance(latitude, longitude, pageRequest);

        // Then
        assertEquals(2, result.getTotalElements()); // Only carPark1 and carPark2 have available lots > 0
        assertTrue(result.getContent().stream().allMatch(cp -> cp.getAvailableLots() > 0));
    }

    @Test
    void testFindNearestCarParksWithDistanceLimit_Success() {
        // Given
        BigDecimal latitude = new BigDecimal("1.23456789");
        BigDecimal longitude = new BigDecimal("103.45678901");
        int limit = 1;

        // When
        List<CarPark> result = carParkRepository.findNearestCarParksWithDistanceLimit(latitude, longitude, limit);

        // Then
        assertEquals(1, result.size()); // Should return only 1 result due to limit
        assertTrue(result.get(0).getAvailableLots() > 0);
    }

    @Test
    void testSaveAndRetrieve_Success() {
        // Given
        CarPark newCarPark = new CarPark("A4", "New Address", new BigDecimal("1.23456792"), new BigDecimal("103.45678904"));
        newCarPark.setTotalLots(150);
        newCarPark.setAvailableLots(75);
        newCarPark.setCarParkType("MULTI-STOREY CAR PARK");
        newCarPark.setTypeOfParkingSystem("ELECTRONIC PARKING");
        newCarPark.setCreatedBy("TEST_USER");

        // When
        CarPark savedCarPark = carParkRepository.save(newCarPark);
        Optional<CarPark> retrievedCarPark = carParkRepository.findByCarParkNoAndDeletedAtIsNull("A4");

        // Then
        assertTrue(retrievedCarPark.isPresent());
        assertEquals("A4", retrievedCarPark.get().getCarParkNo());
        assertEquals("New Address", retrievedCarPark.get().getAddress());
        assertEquals(150, retrievedCarPark.get().getTotalLots());
        assertEquals(75, retrievedCarPark.get().getAvailableLots());
    }

    @Test
    void testUpdateCarPark_Success() {
        // Given
        carPark1.setAvailableLots(25);
        carPark1.setUpdatedBy("TEST_USER_UPDATE");

        // When
        CarPark updatedCarPark = carParkRepository.save(carPark1);
        Optional<CarPark> retrievedCarPark = carParkRepository.findByCarParkNoAndDeletedAtIsNull("A1");

        // Then
        assertTrue(retrievedCarPark.isPresent());
        assertEquals(25, retrievedCarPark.get().getAvailableLots());
        assertEquals("TEST_USER_UPDATE", retrievedCarPark.get().getUpdatedBy());
    }
}
