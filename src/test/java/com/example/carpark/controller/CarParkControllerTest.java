package com.example.carpark.controller;

import com.example.carpark.dto.request.NearestCarParkRequestDTO;
import com.example.carpark.dto.response.CarParkResponseDTO;
import com.example.carpark.service.CachedCarParkService;
import com.example.carpark.service.CarParkAvailabilityService;
import com.example.carpark.service.CarParkStreamingImportService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarParkControllerTest {

    @Mock
    private CachedCarParkService cachedCarParkService;

    @Mock
    private CarParkAvailabilityService carParkAvailabilityService;

    @Mock
    private CarParkStreamingImportService carParkStreamingImportService;

    @InjectMocks
    private CarParkController carParkController;

    private NearestCarParkRequestDTO request;
    private List<CarParkResponseDTO> mockCarParks;

    @BeforeEach
    void setUp() {
        request = new NearestCarParkRequestDTO(
                new BigDecimal("1.3521"),
                new BigDecimal("103.8198"),
                1,
                10);

        CarParkResponseDTO carParkResponseDTO1 = new CarParkResponseDTO(
                "Test Address 1",
                new BigDecimal("1.3521"),
                new BigDecimal("103.8198"),
                100,
                50);

        CarParkResponseDTO carParkResponseDTO2 = new CarParkResponseDTO(
                "Test Address 2",
                new BigDecimal("1.3522"),
                new BigDecimal("103.8199"),
                200,
                100);

        mockCarParks = Arrays.asList(carParkResponseDTO1, carParkResponseDTO2);
    }

    @Test
    void testFindNearestCarParks_Success() {
        // Arrange
        when(cachedCarParkService.findNearestCarParks(request)).thenReturn(mockCarParks);

        // Act
        ResponseEntity<List<CarParkResponseDTO>> response = carParkController.findNearestCarParks(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Test Address 1", response.getBody().get(0).getAddress());
        assertEquals("Test Address 2", response.getBody().get(1).getAddress());

        verify(cachedCarParkService).findNearestCarParks(request);
    }

    @Test
    void testFindNearestCarParks_Exception() {
        // Arrange
        when(cachedCarParkService.findNearestCarParks(request))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            carParkController.findNearestCarParks(request);
        });

        verify(cachedCarParkService).findNearestCarParks(request);
    }

    @Test
    void testImportCarParkData_Success() {
        // Arrange
        doNothing().when(carParkStreamingImportService).importCarParkDataStreaming();

        // Act
        ResponseEntity<String> response = carParkController.importCarParkData();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(carParkStreamingImportService).importCarParkDataStreaming();
    }

    @Test
    void testImportCarParkData_Exception() {
        // Arrange
        doThrow(new RuntimeException("Import error")).when(carParkStreamingImportService).importCarParkDataStreaming();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            carParkController.importCarParkData();
        });

        verify(carParkStreamingImportService).importCarParkDataStreaming();
    }

    @Test
    void testUpdateCarParkAvailability_Success() {
        // Arrange
        doNothing().when(carParkAvailabilityService).updateCarParkAvailability();

        // Act
        ResponseEntity<String> response = carParkController.updateCarParkAvailability();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(carParkAvailabilityService).updateCarParkAvailability();
    }

    @Test
    void testUpdateCarParkAvailability_Exception() {
        // Arrange
        doThrow(new RuntimeException("Update error")).when(carParkAvailabilityService).updateCarParkAvailability();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            carParkController.updateCarParkAvailability();
        });

        verify(carParkAvailabilityService).updateCarParkAvailability();
    }

    @Test
    void testHealthCheck() {
        // Act
        ResponseEntity<String> response = carParkController.health();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
