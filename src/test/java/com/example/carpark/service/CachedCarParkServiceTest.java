package com.example.carpark.service;

import com.example.carpark.dto.request.NearestCarParkRequestDTO;
import com.example.carpark.dto.response.CarParkResponseDTO;
import com.example.carpark.entity.CarPark;
import com.example.carpark.repository.CarParkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CachedCarParkServiceTest {

    @Mock
    private CarParkRepository carParkRepository;

    @InjectMocks
    private CachedCarParkService cachedCarParkService;

    private CarPark carPark1;
    private CarPark carPark2;
    private NearestCarParkRequestDTO request;

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

        request = new NearestCarParkRequestDTO();
        request.setLatitude(new BigDecimal("1.23456789"));
        request.setLongitude(new BigDecimal("103.45678901"));
        request.setPage(1);
        request.setPerPage(10);
    }

    @Test
    void testFindNearestCarParks_Success() {
        // Given
        List<CarPark> carParks = Arrays.asList(carPark1, carPark2);
        Page<CarPark> carParkPage = new PageImpl<>(carParks);
        
        when(carParkRepository.findNearestCarParksWithDistance(
                any(BigDecimal.class), 
                any(BigDecimal.class), 
                any(Pageable.class)
        )).thenReturn(carParkPage);

        // When
        List<CarParkResponseDTO> result = cachedCarParkService.findNearestCarParks(request);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        CarParkResponseDTO firstResult = result.get(0);
        assertEquals("A1", firstResult.getCarParkNo());
        assertEquals("Test Address 1", firstResult.getAddress());
        assertEquals(100, firstResult.getTotalLots());
        assertEquals(50, firstResult.getAvailableLots());

        verify(carParkRepository).findNearestCarParksWithDistance(
                request.getLatitude(),
                request.getLongitude(),
                PageRequest.of(0, 10)
        );
    }

    @Test
    void testFindNearestCarParks_EmptyResult() {
        // Given
        Page<CarPark> emptyPage = new PageImpl<>(Arrays.asList());
        when(carParkRepository.findNearestCarParksWithDistance(
                any(BigDecimal.class), 
                any(BigDecimal.class), 
                any(Pageable.class)
        )).thenReturn(emptyPage);

        // When
        List<CarParkResponseDTO> result = cachedCarParkService.findNearestCarParks(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllCarParksWithAvailability_Success() {
        // Given
        List<CarPark> carParks = Arrays.asList(carPark1, carPark2);
        when(carParkRepository.findAllWithAvailableLots()).thenReturn(carParks);

        // When
        List<CarParkResponseDTO> result = cachedCarParkService.getAllCarParksWithAvailability();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(carParkRepository).findAllWithAvailableLots();
    }

    @Test
    void testFindNearestCarParksWithLimit_Success() {
        // Given
        List<CarPark> carParks = Arrays.asList(carPark1);
        when(carParkRepository.findNearestCarParksWithDistanceLimit(
                any(BigDecimal.class), 
                any(BigDecimal.class), 
                anyInt()
        )).thenReturn(carParks);

        // When
        List<CarParkResponseDTO> result = cachedCarParkService.findNearestCarParksWithLimit(
                new BigDecimal("1.23456789"), 
                new BigDecimal("103.45678901"), 
                5
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A1", result.get(0).getCarParkNo());
    }
}
