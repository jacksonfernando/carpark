package com.example.carpark.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoordinateConversionServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private CoordinateConversionService coordinateConversionService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        coordinateConversionService = new CoordinateConversionService();
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(coordinateConversionService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(coordinateConversionService, "onemapApiUrl", "https://developers.onemap.sg/commonapi/convert/3414to4326");
        ReflectionTestUtils.setField(coordinateConversionService, "webClient", webClient);
    }

    @Test
    void testConvertSVY21ToWGS84_WebClientException() {
        // Given
        double xCoord = 30000.0;
        double yCoord = 30000.0;

        when(webClient.get()).thenThrow(new RuntimeException("Network error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            coordinateConversionService.convertSVY21ToWGS84(xCoord, yCoord);
        });
    }

    @Test
    void testCalculateDistance_SamePoint() {
        // Given
        double lat1 = 1.23456789;
        double lon1 = 103.45678901;
        double lat2 = 1.23456789;
        double lon2 = 103.45678901;

        // When
        double result = coordinateConversionService.calculateDistance(lat1, lon1, lat2, lon2);

        // Then
        assertEquals(0.0, result, 0.001); // Should be very close to 0
    }

    @Test
    void testCalculateDistance_DifferentPoints() {
        // Given
        double lat1 = 1.23456789;
        double lon1 = 103.45678901;
        double lat2 = 1.24456789; // Slightly different latitude
        double lon2 = 103.46678901; // Slightly different longitude

        // When
        double result = coordinateConversionService.calculateDistance(lat1, lon1, lat2, lon2);

        // Then
        assertTrue(result > 0); // Should be positive distance
        assertTrue(result < 10); // Should be less than 10 km for small differences
    }

    @Test
    void testCalculateDistance_LargeDistance() {
        // Given
        double lat1 = 1.23456789;
        double lon1 = 103.45678901;
        double lat2 = 1.33456789; // 0.1 degree difference in latitude
        double lon2 = 103.55678901; // 0.1 degree difference in longitude

        // When
        double result = coordinateConversionService.calculateDistance(lat1, lon1, lat2, lon2);

        // Then
        assertTrue(result > 10); // Should be more than 10 km
    }

    @Test
    void testCalculateDistance_EdgeCases() {
        // Test with extreme coordinates
        double result1 = coordinateConversionService.calculateDistance(90.0, 180.0, -90.0, -180.0);
        assertTrue(result1 > 0);

        // Test with zero coordinates
        double result2 = coordinateConversionService.calculateDistance(0.0, 0.0, 0.0, 0.0);
        assertEquals(0.0, result2, 0.001);
    }

    @Test
    void testCalculateDistance_Accuracy() {
        // Test known distance calculation
        // Singapore to Kuala Lumpur approximate coordinates
        double singaporeLat = 1.3521;
        double singaporeLon = 103.8198;
        double klLat = 3.1390;
        double klLon = 101.6869;

        double result = coordinateConversionService.calculateDistance(singaporeLat, singaporeLon, klLat, klLon);

        // Distance should be approximately 300-350 km
        assertTrue(result > 300 && result < 350);
    }
}
