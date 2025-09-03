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
    private RedisGeospatialService redisGeospatialService;

    @Mock
    private CoordinateConversionService coordinateConversionService;

    @Test
    void testServiceInitialization() {
        // Arrange & Act
        CarParkStreamingImportService service = new CarParkStreamingImportService(
                carParkMySqlRepository,
                redisGeospatialService,
                null, // GeometryFactory not needed for basic tests
                coordinateConversionService);

        // Assert
        assertNotNull(service);
    }

    @Test
    void testConstructorInjection() {
        // Arrange & Act
        CarParkStreamingImportService service = new CarParkStreamingImportService(
                carParkMySqlRepository,
                redisGeospatialService,
                null,
                coordinateConversionService);

        // Assert
        assertNotNull(service);
    }
}
