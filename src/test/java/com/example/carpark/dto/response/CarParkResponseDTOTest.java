package com.example.carpark.dto.response;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CarParkResponseDTOTest {

    private CarParkResponseDTO carParkResponseDTO;

    @BeforeEach
    void setUp() {
        carParkResponseDTO = new CarParkResponseDTO(
            "Test Address",
            new BigDecimal("1.3521"),
            new BigDecimal("103.8198"),
            100,
            50
        );
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("Test Address", carParkResponseDTO.getAddress());
        assertEquals(new BigDecimal("1.3521"), carParkResponseDTO.getLatitude());
        assertEquals(new BigDecimal("103.8198"), carParkResponseDTO.getLongitude());
        assertEquals(100, carParkResponseDTO.getTotalLots());
        assertEquals(50, carParkResponseDTO.getAvailableLots());
    }

    @Test
    void testEquals() {
        CarParkResponseDTO sameDto = new CarParkResponseDTO(
            "Test Address",
            new BigDecimal("1.3521"),
            new BigDecimal("103.8198"),
            100,
            50
        );
        CarParkResponseDTO differentDto = new CarParkResponseDTO(
            "Different Address",
            new BigDecimal("1.3521"),
            new BigDecimal("103.8198"),
            100,
            50
        );

        assertEquals(carParkResponseDTO, sameDto);
        assertNotEquals(carParkResponseDTO, differentDto);
        assertNotEquals(carParkResponseDTO, null);
        assertEquals(carParkResponseDTO, carParkResponseDTO);
    }

    @Test
    void testHashCode() {
        CarParkResponseDTO sameDto = new CarParkResponseDTO(
            "Test Address",
            new BigDecimal("1.3521"),
            new BigDecimal("103.8198"),
            100,
            50
        );

        assertEquals(carParkResponseDTO.hashCode(), sameDto.hashCode());
    }

    @Test
    void testToString() {
        String toString = carParkResponseDTO.toString();
        assertTrue(toString.contains("Test Address"));
        assertTrue(toString.contains("1.3521"));
        assertTrue(toString.contains("103.8198"));
        assertTrue(toString.contains("100"));
        assertTrue(toString.contains("50"));
    }
}
