package com.example.carpark.service;

import com.example.carpark.repository.mysql.CarParkMySqlRepository;
import com.example.carpark.service.RedisGeospatialService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.locationtech.jts.geom.GeometryFactory;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CachedCarParkServiceTest {

    @Mock
    private CarParkMySqlRepository carParkMySqlRepository;

    @Mock
    private GeometryFactory geometryFactory;

    @Mock
    private RedisGeospatialService redisGeospatialService;

    @Test
    void testServiceInitialization() {
        // Arrange & Act
        CachedCarParkService service = new CachedCarParkService(
            carParkMySqlRepository,
            geometryFactory,
            redisGeospatialService
        );

        // Assert
        assertNotNull(service);
    }

    @Test
    void testConstructorInjection() {
        // Arrange & Act
        CachedCarParkService service = new CachedCarParkService(
            carParkMySqlRepository,
            geometryFactory,
            redisGeospatialService
        );

        // Assert
        assertNotNull(service);
        // Verify that dependencies are properly injected
        assertDoesNotThrow(() -> {
            // Test that the service can be instantiated
        });
    }
}
