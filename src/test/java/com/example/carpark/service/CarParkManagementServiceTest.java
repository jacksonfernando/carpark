package com.example.carpark.service;

import com.example.carpark.entity.CarPark;
import com.example.carpark.repository.CarParkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarParkManagementServiceTest {

    @Mock
    private CarParkRepository carParkRepository;

    @InjectMocks
    private CarParkManagementService carParkManagementService;

    private CarPark carPark1;
    private CarPark carPark2;

    @BeforeEach
    void setUp() {
        carPark1 = new CarPark("A1", "Test Address 1", new BigDecimal("1.23456789"), new BigDecimal("103.45678901"));
        carPark1.setId(1L);
        carPark1.setTotalLots(100);
        carPark1.setAvailableLots(50);
        carPark1.setCarParkType("SURFACE CAR PARK");
        carPark1.setTypeOfParkingSystem("COUPON");

        carPark2 = new CarPark("A2", "Test Address 2", new BigDecimal("1.23456790"), new BigDecimal("103.45678902"));
        carPark2.setId(2L);
        carPark2.setTotalLots(80);
        carPark2.setAvailableLots(30);
        carPark2.setCarParkType("MULTI-STOREY CAR PARK");
        carPark2.setTypeOfParkingSystem("ELECTRONIC PARKING");
    }

    @Test
    void testSoftDeleteCarPark_Success() {
        // Given
        String carParkNo = "A1";
        String deletedBy = "TEST_USER";
        when(carParkRepository.findByCarParkNoAndDeletedAtIsNull(carParkNo)).thenReturn(Optional.of(carPark1));
        when(carParkRepository.save(any(CarPark.class))).thenReturn(carPark1);

        // When
        boolean result = carParkManagementService.softDeleteCarPark(carParkNo, deletedBy);

        // Then
        assertTrue(result);
        verify(carParkRepository).findByCarParkNoAndDeletedAtIsNull(carParkNo);
        verify(carParkRepository).save(any(CarPark.class));
    }

    @Test
    void testSoftDeleteCarPark_NotFound() {
        // Given
        String carParkNo = "NON_EXISTENT";
        String deletedBy = "TEST_USER";
        when(carParkRepository.findByCarParkNoAndDeletedAtIsNull(carParkNo)).thenReturn(Optional.empty());

        // When
        boolean result = carParkManagementService.softDeleteCarPark(carParkNo, deletedBy);

        // Then
        assertFalse(result);
        verify(carParkRepository).findByCarParkNoAndDeletedAtIsNull(carParkNo);
        verify(carParkRepository, never()).save(any(CarPark.class));
    }

    @Test
    void testRestoreCarPark_Success() {
        // Given
        String carParkNo = "A1";
        String restoredBy = "TEST_USER";
        carPark1.softDelete(); // Mark as deleted
        when(carParkRepository.findByCarParkNo(carParkNo)).thenReturn(Optional.of(carPark1));
        when(carParkRepository.save(any(CarPark.class))).thenReturn(carPark1);

        // When
        boolean result = carParkManagementService.restoreCarPark(carParkNo, restoredBy);

        // Then
        assertTrue(result);
        verify(carParkRepository).findByCarParkNo(carParkNo);
        verify(carParkRepository).save(any(CarPark.class));
    }

    @Test
    void testRestoreCarPark_NotDeleted() {
        // Given
        String carParkNo = "A1";
        String restoredBy = "TEST_USER";
        when(carParkRepository.findByCarParkNo(carParkNo)).thenReturn(Optional.of(carPark1));

        // When
        boolean result = carParkManagementService.restoreCarPark(carParkNo, restoredBy);

        // Then
        assertFalse(result);
        verify(carParkRepository).findByCarParkNo(carParkNo);
        verify(carParkRepository, never()).save(any(CarPark.class));
    }

    @Test
    void testRestoreCarPark_NotFound() {
        // Given
        String carParkNo = "NON_EXISTENT";
        String restoredBy = "TEST_USER";
        when(carParkRepository.findByCarParkNo(carParkNo)).thenReturn(Optional.empty());

        // When
        boolean result = carParkManagementService.restoreCarPark(carParkNo, restoredBy);

        // Then
        assertFalse(result);
        verify(carParkRepository).findByCarParkNo(carParkNo);
        verify(carParkRepository, never()).save(any(CarPark.class));
    }

    @Test
    void testGetCarParkByNumber_Success() {
        // Given
        String carParkNo = "A1";
        when(carParkRepository.findByCarParkNo(carParkNo)).thenReturn(Optional.of(carPark1));

        // When
        Optional<CarPark> result = carParkManagementService.getCarParkByNumber(carParkNo);

        // Then
        assertTrue(result.isPresent());
        assertEquals("A1", result.get().getCarParkNo());
        verify(carParkRepository).findByCarParkNo(carParkNo);
    }

    @Test
    void testGetActiveCarParkByNumber_Success() {
        // Given
        String carParkNo = "A1";
        when(carParkRepository.findByCarParkNoAndDeletedAtIsNull(carParkNo)).thenReturn(Optional.of(carPark1));

        // When
        Optional<CarPark> result = carParkManagementService.getActiveCarParkByNumber(carParkNo);

        // Then
        assertTrue(result.isPresent());
        assertEquals("A1", result.get().getCarParkNo());
        verify(carParkRepository).findByCarParkNoAndDeletedAtIsNull(carParkNo);
    }

    @Test
    void testGetAllActiveCarParks_Success() {
        // Given
        List<CarPark> carParks = Arrays.asList(carPark1, carPark2);
        when(carParkRepository.findByDeletedAtIsNull()).thenReturn(carParks);

        // When
        List<CarPark> result = carParkManagementService.getAllActiveCarParks();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(carParkRepository).findByDeletedAtIsNull();
    }

    @Test
    void testGetCarParksByType_Success() {
        // Given
        String carParkType = "SURFACE CAR PARK";
        List<CarPark> carParks = Arrays.asList(carPark1);
        when(carParkRepository.findByCarParkTypeAndDeletedAtIsNull(carParkType)).thenReturn(carParks);

        // When
        List<CarPark> result = carParkManagementService.getCarParksByType(carParkType);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SURFACE CAR PARK", result.get(0).getCarParkType());
        verify(carParkRepository).findByCarParkTypeAndDeletedAtIsNull(carParkType);
    }

    @Test
    void testGetCarParksByParkingSystem_Success() {
        // Given
        String parkingSystemType = "COUPON";
        List<CarPark> carParks = Arrays.asList(carPark1);
        when(carParkRepository.findByTypeOfParkingSystemAndDeletedAtIsNull(parkingSystemType)).thenReturn(carParks);

        // When
        List<CarPark> result = carParkManagementService.getCarParksByParkingSystem(parkingSystemType);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("COUPON", result.get(0).getTypeOfParkingSystem());
        verify(carParkRepository).findByTypeOfParkingSystemAndDeletedAtIsNull(parkingSystemType);
    }

    @Test
    void testCountAvailableCarParks_Success() {
        // Given
        when(carParkRepository.countAvailableCarParks()).thenReturn(5L);

        // When
        long result = carParkManagementService.countAvailableCarParks();

        // Then
        assertEquals(5L, result);
        verify(carParkRepository).countAvailableCarParks();
    }

    @Test
    void testGetCarParksCreatedAfter_Success() {
        // Given
        LocalDateTime date = LocalDateTime.now().minusDays(1);
        List<CarPark> carParks = Arrays.asList(carPark1);
        when(carParkRepository.findByCreatedAtAfterAndDeletedAtIsNull(date)).thenReturn(carParks);

        // When
        List<CarPark> result = carParkManagementService.getCarParksCreatedAfter(date);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(carParkRepository).findByCreatedAtAfterAndDeletedAtIsNull(date);
    }

    @Test
    void testGetCarParksUpdatedAfter_Success() {
        // Given
        LocalDateTime date = LocalDateTime.now().minusDays(1);
        List<CarPark> carParks = Arrays.asList(carPark1);
        when(carParkRepository.findByUpdatedAtAfterAndDeletedAtIsNull(date)).thenReturn(carParks);

        // When
        List<CarPark> result = carParkManagementService.getCarParksUpdatedAfter(date);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(carParkRepository).findByUpdatedAtAfterAndDeletedAtIsNull(date);
    }

    @Test
    void testUpdateCarParkAvailability_Success() {
        // Given
        String carParkNo = "A1";
        Integer totalLots = 120;
        Integer availableLots = 60;
        String updatedBy = "TEST_USER";
        when(carParkRepository.findByCarParkNoAndDeletedAtIsNull(carParkNo)).thenReturn(Optional.of(carPark1));
        when(carParkRepository.save(any(CarPark.class))).thenReturn(carPark1);

        // When
        boolean result = carParkManagementService.updateCarParkAvailability(carParkNo, totalLots, availableLots, updatedBy);

        // Then
        assertTrue(result);
        verify(carParkRepository).findByCarParkNoAndDeletedAtIsNull(carParkNo);
        verify(carParkRepository).save(any(CarPark.class));
    }

    @Test
    void testUpdateCarParkAvailability_NotFound() {
        // Given
        String carParkNo = "NON_EXISTENT";
        Integer totalLots = 120;
        Integer availableLots = 60;
        String updatedBy = "TEST_USER";
        when(carParkRepository.findByCarParkNoAndDeletedAtIsNull(carParkNo)).thenReturn(Optional.empty());

        // When
        boolean result = carParkManagementService.updateCarParkAvailability(carParkNo, totalLots, availableLots, updatedBy);

        // Then
        assertFalse(result);
        verify(carParkRepository).findByCarParkNoAndDeletedAtIsNull(carParkNo);
        verify(carParkRepository, never()).save(any(CarPark.class));
    }
}
