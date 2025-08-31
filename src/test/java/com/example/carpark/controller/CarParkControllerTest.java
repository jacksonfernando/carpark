package com.example.carpark.controller;

import com.example.carpark.dto.request.NearestCarParkRequestDTO;
import com.example.carpark.dto.response.CarParkResponseDTO;
import com.example.carpark.service.CachedCarParkService;
import com.example.carpark.service.CarParkAvailabilityService;
import com.example.carpark.service.CarParkDataImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CarParkControllerTest {

    @Mock
    private CachedCarParkService cachedCarParkService;

    @Mock
    private CarParkDataImportService carParkDataImportService;

    @Mock
    private CarParkAvailabilityService carParkAvailabilityService;

    @InjectMocks
    private CarParkController carParkController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private CarParkResponseDTO carParkResponseDTO1;
    private CarParkResponseDTO carParkResponseDTO2;
    private NearestCarParkRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(carParkController).build();
        objectMapper = new ObjectMapper();

        // Setup test data
        carParkResponseDTO1 = new CarParkResponseDTO(
                "Test Address 1",
                new BigDecimal("1.23456789"),
                new BigDecimal("103.45678901"),
                100,
                50
        );
        carParkResponseDTO1.setCarParkNo("A1");
        carParkResponseDTO1.setCarParkType("SURFACE CAR PARK");

        carParkResponseDTO2 = new CarParkResponseDTO(
                "Test Address 2",
                new BigDecimal("1.23456790"),
                new BigDecimal("103.45678902"),
                80,
                30
        );
        carParkResponseDTO2.setCarParkNo("A2");
        carParkResponseDTO2.setCarParkType("MULTI-STOREY CAR PARK");

        requestDTO = new NearestCarParkRequestDTO();
        requestDTO.setLatitude(new BigDecimal("1.23456789"));
        requestDTO.setLongitude(new BigDecimal("103.45678901"));
        requestDTO.setPage(1);
        requestDTO.setPerPage(10);
    }

    @Test
    void testFindNearestCarParks_Success() throws Exception {
        // Given
        List<CarParkResponseDTO> expectedResponse = Arrays.asList(carParkResponseDTO1, carParkResponseDTO2);
        when(cachedCarParkService.findNearestCarParks(any(NearestCarParkRequestDTO.class)))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<List<CarParkResponseDTO>> response = carParkController.findNearestCarParks(requestDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("A1", response.getBody().get(0).getCarParkNo());
        assertEquals("A2", response.getBody().get(1).getCarParkNo());

        verify(cachedCarParkService).findNearestCarParks(requestDTO);
    }

    @Test
    void testFindNearestCarParks_Exception() {
        // Given
        when(cachedCarParkService.findNearestCarParks(any(NearestCarParkRequestDTO.class)))
                .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<List<CarParkResponseDTO>> response = carParkController.findNearestCarParks(requestDTO);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());

        verify(cachedCarParkService).findNearestCarParks(requestDTO);
    }

    @Test
    void testGetAllCarParksWithAvailability_Success() throws Exception {
        // Given
        List<CarParkResponseDTO> expectedResponse = Arrays.asList(carParkResponseDTO1, carParkResponseDTO2);
        when(cachedCarParkService.getAllCarParksWithAvailability()).thenReturn(expectedResponse);

        // When
        ResponseEntity<List<CarParkResponseDTO>> response = carParkController.getAllCarParksWithAvailability();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(cachedCarParkService).getAllCarParksWithAvailability();
    }

    @Test
    void testGetAllCarParksWithAvailability_Exception() {
        // Given
        when(cachedCarParkService.getAllCarParksWithAvailability())
                .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<List<CarParkResponseDTO>> response = carParkController.getAllCarParksWithAvailability();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());

        verify(cachedCarParkService).getAllCarParksWithAvailability();
    }

    @Test
    void testImportCarParkData_Success() throws Exception {
        // Given
        doNothing().when(carParkDataImportService).importCarParkData();

        // When
        ResponseEntity<String> response = carParkController.importCarParkData();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Car park data import completed successfully", response.getBody());

        verify(carParkDataImportService).importCarParkData();
    }

    @Test
    void testImportCarParkData_Exception() {
        // Given
        doThrow(new RuntimeException("Import failed")).when(carParkDataImportService).importCarParkData();

        // When
        ResponseEntity<String> response = carParkController.importCarParkData();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to import car park data"));

        verify(carParkDataImportService).importCarParkData();
    }

    @Test
    void testUpdateCarParkAvailability_Success() throws Exception {
        // Given
        doNothing().when(carParkAvailabilityService).updateCarParkAvailability();

        // When
        ResponseEntity<String> response = carParkController.updateCarParkAvailability();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Car park availability update completed successfully", response.getBody());

        verify(carParkAvailabilityService).updateCarParkAvailability();
    }

    @Test
    void testUpdateCarParkAvailability_Exception() {
        // Given
        doThrow(new RuntimeException("Update failed")).when(carParkAvailabilityService).updateCarParkAvailability();

        // When
        ResponseEntity<String> response = carParkController.updateCarParkAvailability();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to update car park availability"));

        verify(carParkAvailabilityService).updateCarParkAvailability();
    }

    @Test
    void testHealth_Success() throws Exception {
        // When
        ResponseEntity<String> response = carParkController.health();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Car Park API is running", response.getBody());
    }

    @Test
    void testFindNearestCarParks_WithNullLatitude() {
        // Given - Invalid request with null latitude
        NearestCarParkRequestDTO invalidRequest = new NearestCarParkRequestDTO();
        invalidRequest.setLatitude(null);
        invalidRequest.setLongitude(new BigDecimal("103.45678901"));
        invalidRequest.setPage(1);
        invalidRequest.setPerPage(10);

        // When & Then - Should handle null values gracefully
        when(cachedCarParkService.findNearestCarParks(any(NearestCarParkRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid coordinates"));

        ResponseEntity<List<CarParkResponseDTO>> response = carParkController.findNearestCarParks(invalidRequest);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testFindNearestCarParks_WithInvalidCoordinates() {
        // Given - Invalid coordinates
        NearestCarParkRequestDTO invalidRequest = new NearestCarParkRequestDTO();
        invalidRequest.setLatitude(new BigDecimal("100.0")); // Invalid latitude > 90
        invalidRequest.setLongitude(new BigDecimal("103.45678901"));
        invalidRequest.setPage(1);
        invalidRequest.setPerPage(10);

        // When & Then - Should handle invalid coordinates gracefully
        when(cachedCarParkService.findNearestCarParks(any(NearestCarParkRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid coordinates"));

        ResponseEntity<List<CarParkResponseDTO>> response = carParkController.findNearestCarParks(invalidRequest);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testFindNearestCarParks_WithInvalidPagination() {
        // Given - Invalid pagination
        NearestCarParkRequestDTO invalidRequest = new NearestCarParkRequestDTO();
        invalidRequest.setLatitude(new BigDecimal("1.23456789"));
        invalidRequest.setLongitude(new BigDecimal("103.45678901"));
        invalidRequest.setPage(0); // Invalid page < 1
        invalidRequest.setPerPage(10);

        // When & Then - Should handle invalid pagination gracefully
        when(cachedCarParkService.findNearestCarParks(any(NearestCarParkRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid pagination"));

        ResponseEntity<List<CarParkResponseDTO>> response = carParkController.findNearestCarParks(invalidRequest);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
