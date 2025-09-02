package com.example.carpark.service;

import com.example.carpark.repository.mysql.CarParkMySqlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CarParkDataImportServiceTest {

    @Mock
    private CarParkMySqlRepository carParkMySqlRepository;

    @Mock
    private CoordinateConversionService coordinateConversionService;

    @Test
    void testServiceInitialization() {
        // Arrange & Act
        CarParkDataImportService service = new CarParkDataImportService(
            carParkMySqlRepository,
            coordinateConversionService
        );

        // Assert
        assertNotNull(service);
    }

    @Test
    void testConstructorInjection() {
        // Arrange & Act
        CarParkDataImportService service = new CarParkDataImportService(
            carParkMySqlRepository,
            coordinateConversionService
        );

        // Assert
        assertNotNull(service);
    }
}
