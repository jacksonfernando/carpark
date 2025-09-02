package com.example.carpark.service;

import com.example.carpark.entity.CarPark;
import com.example.carpark.repository.mysql.CarParkMySqlRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarParkManagementServiceTest {

    @Mock
    private CarParkMySqlRepository carParkMySqlRepository;

    @InjectMocks
    private CarParkManagementService carParkManagementService;

    private CarPark carPark1;
    private CarPark carPark2;

    @BeforeEach
    void setUp() {
        // Create mock car parks
        carPark1 = new CarPark();
        carPark1.setId(1L);
        carPark1.setCarParkNo("A1");
        carPark1.setAddress("Test Address 1");
        carPark1.setLatitude(new BigDecimal("1.3521"));
        carPark1.setLongitude(new BigDecimal("103.8198"));
        carPark1.setTotalLots(100);
        carPark1.setAvailableLots(50);

        carPark2 = new CarPark();
        carPark2.setId(2L);
        carPark2.setCarParkNo("A2");
        carPark2.setAddress("Test Address 2");
        carPark2.setLatitude(new BigDecimal("1.3522"));
        carPark2.setLongitude(new BigDecimal("103.8199"));
        carPark2.setTotalLots(200);
        carPark2.setAvailableLots(100);
    }

    @Test
    void testSoftDeleteCarPark_Success() {
        // Arrange
        when(carParkMySqlRepository.findByCarParkNoAndDeletedAtIsNull("A1")).thenReturn(Optional.of(carPark1));
        when(carParkMySqlRepository.save(any(CarPark.class))).thenReturn(carPark1);

        // Act
        boolean result = carParkManagementService.softDeleteCarPark("A1", "TEST_USER");

        // Assert
        assertTrue(result);
        verify(carParkMySqlRepository).findByCarParkNoAndDeletedAtIsNull("A1");
        verify(carParkMySqlRepository).save(any(CarPark.class));
    }

    @Test
    void testSoftDeleteCarPark_NotFound() {
        // Arrange
        when(carParkMySqlRepository.findByCarParkNoAndDeletedAtIsNull("NON_EXISTENT")).thenReturn(Optional.empty());

        // Act
        boolean result = carParkManagementService.softDeleteCarPark("NON_EXISTENT", "TEST_USER");

        // Assert
        assertFalse(result);
        verify(carParkMySqlRepository).findByCarParkNoAndDeletedAtIsNull("NON_EXISTENT");
        verify(carParkMySqlRepository, never()).save(any(CarPark.class));
    }

    @Test
    void testRestoreCarPark_Success() {
        // Arrange
        carPark1.softDelete(); // Mark as deleted
        when(carParkMySqlRepository.findByCarParkNo("A1")).thenReturn(Optional.of(carPark1));
        when(carParkMySqlRepository.save(any(CarPark.class))).thenReturn(carPark1);

        // Act
        boolean result = carParkManagementService.restoreCarPark("A1", "TEST_USER");

        // Assert
        assertTrue(result);
        verify(carParkMySqlRepository).findByCarParkNo("A1");
        verify(carParkMySqlRepository).save(any(CarPark.class));
    }

    @Test
    void testRestoreCarPark_NotDeleted() {
        // Arrange
        when(carParkMySqlRepository.findByCarParkNo("A1")).thenReturn(Optional.of(carPark1));

        // Act
        boolean result = carParkManagementService.restoreCarPark("A1", "TEST_USER");

        // Assert
        assertFalse(result);
        verify(carParkMySqlRepository).findByCarParkNo("A1");
        verify(carParkMySqlRepository, never()).save(any(CarPark.class));
    }

    @Test
    void testRestoreCarPark_NotFound() {
        // Arrange
        when(carParkMySqlRepository.findByCarParkNo("NON_EXISTENT")).thenReturn(Optional.empty());

        // Act
        boolean result = carParkManagementService.restoreCarPark("NON_EXISTENT", "TEST_USER");

        // Assert
        assertFalse(result);
        verify(carParkMySqlRepository).findByCarParkNo("NON_EXISTENT");
        verify(carParkMySqlRepository, never()).save(any(CarPark.class));
    }

    @Test
    void testGetCarParkByNumber_Success() {
        // Arrange
        when(carParkMySqlRepository.findByCarParkNo("A1")).thenReturn(Optional.of(carPark1));

        // Act
        Optional<CarPark> result = carParkManagementService.getCarParkByNumber("A1");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("A1", result.get().getCarParkNo());
        verify(carParkMySqlRepository).findByCarParkNo("A1");
    }

    @Test
    void testGetActiveCarParkByNumber_Success() {
        // Arrange
        when(carParkMySqlRepository.findByCarParkNoAndDeletedAtIsNull("A1")).thenReturn(Optional.of(carPark1));

        // Act
        Optional<CarPark> result = carParkManagementService.getActiveCarParkByNumber("A1");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("A1", result.get().getCarParkNo());
        verify(carParkMySqlRepository).findByCarParkNoAndDeletedAtIsNull("A1");
    }

    @Test
    void testGetAllActiveCarParks_Success() {
        // Arrange
        List<CarPark> mockCarParks = Arrays.asList(carPark1, carPark2);
        when(carParkMySqlRepository.findAllActive()).thenReturn(mockCarParks);

        // Act
        List<CarPark> result = carParkManagementService.getAllActiveCarParks();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(carParkMySqlRepository).findAllActive();
    }

    @Test
    void testGetCarParksByType_Success() {
        // Arrange
        List<CarPark> mockCarParks = Arrays.asList(carPark1);
        when(carParkMySqlRepository.findByCarParkType("SURFACE CAR PARK")).thenReturn(mockCarParks);

        // Act
        List<CarPark> result = carParkManagementService.getCarParksByType("SURFACE CAR PARK");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(carParkMySqlRepository).findByCarParkType("SURFACE CAR PARK");
    }

    @Test
    void testGetCarParksByParkingSystem_Success() {
        // Arrange
        List<CarPark> mockCarParks = Arrays.asList(carPark1);
        when(carParkMySqlRepository.findByShortTermParking("COUPON")).thenReturn(mockCarParks);

        // Act
        List<CarPark> result = carParkManagementService.getCarParksByParkingSystem("COUPON");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(carParkMySqlRepository).findByShortTermParking("COUPON");
    }

    @Test
    void testCountAvailableCarParks_Success() {
        // Arrange
        when(carParkMySqlRepository.countCarParksWithAvailability()).thenReturn(5L);

        // Act
        long result = carParkManagementService.countAvailableCarParks();

        // Assert
        assertEquals(5L, result);
        verify(carParkMySqlRepository).countCarParksWithAvailability();
    }

    @Test
    void testUpdateCarParkAvailability_Success() {
        // Arrange
        when(carParkMySqlRepository.findByCarParkNoAndDeletedAtIsNull("A1")).thenReturn(Optional.of(carPark1));
        when(carParkMySqlRepository.save(any(CarPark.class))).thenReturn(carPark1);

        // Act
        boolean result = carParkManagementService.updateCarParkAvailability("A1", 120, 60, "TEST_USER");

        // Assert
        assertTrue(result);
        verify(carParkMySqlRepository).findByCarParkNoAndDeletedAtIsNull("A1");
        verify(carParkMySqlRepository).save(any(CarPark.class));
    }

    @Test
    void testUpdateCarParkAvailability_NotFound() {
        // Arrange
        when(carParkMySqlRepository.findByCarParkNoAndDeletedAtIsNull("NON_EXISTENT")).thenReturn(Optional.empty());

        // Act
        boolean result = carParkManagementService.updateCarParkAvailability("NON_EXISTENT", 120, 60, "TEST_USER");

        // Assert
        assertFalse(result);
        verify(carParkMySqlRepository).findByCarParkNoAndDeletedAtIsNull("NON_EXISTENT");
        verify(carParkMySqlRepository, never()).save(any(CarPark.class));
    }
}
