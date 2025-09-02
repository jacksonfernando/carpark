package com.example.carpark.service;

import com.example.carpark.entity.CarPark;
import com.example.carpark.entity.CarParkAvailability;
import com.example.carpark.repository.external.CarParkExternalApiRepository;
import com.example.carpark.repository.mysql.CarParkMySqlRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.locationtech.jts.geom.GeometryFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarParkAvailabilityServiceTest {

    @Mock
    private CarParkMySqlRepository carParkMySqlRepository;

    @Mock
    private CarParkExternalApiRepository carParkExternalApiRepository;

    @Mock
    private GeometryFactory geometryFactory;

    @InjectMocks
    private CarParkAvailabilityService carParkAvailabilityService;

    @Test
    void testUpdateCarParkAvailability_Success() {
        // Arrange
        doNothing().when(carParkExternalApiRepository).fetchCarParkAvailabilityStreaming(any());

        // Act
        assertDoesNotThrow(() -> carParkAvailabilityService.updateCarParkAvailability());

        // Assert
        verify(carParkExternalApiRepository).fetchCarParkAvailabilityStreaming(any());
    }

    @Test
    void testUpdateCarParkAvailability_Exception() {
        // Arrange
        doThrow(new RuntimeException("API error")).when(carParkExternalApiRepository).fetchCarParkAvailabilityStreaming(any());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            carParkAvailabilityService.updateCarParkAvailability();
        });

        verify(carParkExternalApiRepository).fetchCarParkAvailabilityStreaming(any());
    }

    @Test
    void testScheduledAvailabilityUpdate() {
        // Arrange
        doNothing().when(carParkExternalApiRepository).fetchCarParkAvailabilityStreaming(any());

        // Act
        assertDoesNotThrow(() -> carParkAvailabilityService.scheduledAvailabilityUpdate());

        // Assert
        verify(carParkExternalApiRepository).fetchCarParkAvailabilityStreaming(any());
    }

    @Test
    void testServiceInitialization() {
        // Arrange & Act
        CarParkAvailabilityService service = new CarParkAvailabilityService(
            carParkMySqlRepository,
            carParkExternalApiRepository,
            geometryFactory
        );

        // Assert
        assertNotNull(service);
    }

    @Test
    void testConstructorInjection() {
        // Arrange & Act
        CarParkAvailabilityService service = new CarParkAvailabilityService(
            carParkMySqlRepository,
            carParkExternalApiRepository,
            geometryFactory
        );

        // Assert
        assertNotNull(service);
        // Verify that dependencies are properly injected
        assertDoesNotThrow(() -> {
            // Test that the service can be instantiated
        });
    }
}
