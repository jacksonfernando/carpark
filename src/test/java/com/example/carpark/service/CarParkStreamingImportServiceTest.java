package com.example.carpark.service;

import com.example.carpark.repository.mysql.CarParkMySqlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CarParkStreamingImportServiceTest {

    @Mock
    private CarParkMySqlRepository carParkMySqlRepository;

    @Mock
    private CoordinateConversionService coordinateConversionService;

    @Test
    void testServiceInitialization() {
        // Arrange & Act
        CarParkStreamingImportService service = new CarParkStreamingImportService(
            carParkMySqlRepository,
            coordinateConversionService,
            null // GeometryFactory not needed for basic tests
        );

        // Assert
        assertNotNull(service);
    }

    @Test
    void testConstructorInjection() {
        // Arrange & Act
        CarParkStreamingImportService service = new CarParkStreamingImportService(
            carParkMySqlRepository,
            coordinateConversionService,
            null
        );

        // Assert
        assertNotNull(service);
    }
}
