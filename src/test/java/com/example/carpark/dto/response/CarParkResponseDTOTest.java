package com.example.carpark.dto.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CarParkResponseDTOTest {

    private CarParkResponseDTO carParkResponseDTO;

    @BeforeEach
    void setUp() {
        carParkResponseDTO = new CarParkResponseDTO();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(carParkResponseDTO);
        assertNull(carParkResponseDTO.getAddress());
        assertNull(carParkResponseDTO.getLatitude());
        assertNull(carParkResponseDTO.getLongitude());
        assertNull(carParkResponseDTO.getTotalLots());
        assertNull(carParkResponseDTO.getAvailableLots());
    }

    @Test
    void testParameterizedConstructor() {
        String address = "Test Address";
        BigDecimal latitude = new BigDecimal("1.23456789");
        BigDecimal longitude = new BigDecimal("103.45678901");
        Integer totalLots = 100;
        Integer availableLots = 50;

        CarParkResponseDTO dto = new CarParkResponseDTO(address, latitude, longitude, totalLots, availableLots);

        assertEquals(address, dto.getAddress());
        assertEquals(latitude, dto.getLatitude());
        assertEquals(longitude, dto.getLongitude());
        assertEquals(totalLots, dto.getTotalLots());
        assertEquals(availableLots, dto.getAvailableLots());
    }

    @Test
    void testSettersAndGetters() {
        // Basic fields
        carParkResponseDTO.setAddress("Test Address");
        carParkResponseDTO.setLatitude(new BigDecimal("1.23456789"));
        carParkResponseDTO.setLongitude(new BigDecimal("103.45678901"));
        carParkResponseDTO.setTotalLots(100);
        carParkResponseDTO.setAvailableLots(50);

        assertEquals("Test Address", carParkResponseDTO.getAddress());
        assertEquals(new BigDecimal("1.23456789"), carParkResponseDTO.getLatitude());
        assertEquals(new BigDecimal("103.45678901"), carParkResponseDTO.getLongitude());
        assertEquals(100, carParkResponseDTO.getTotalLots());
        assertEquals(50, carParkResponseDTO.getAvailableLots());

        // Additional fields
        carParkResponseDTO.setCarParkNo("A1");
        carParkResponseDTO.setCarParkType("SURFACE CAR PARK");
        carParkResponseDTO.setTypeOfParkingSystem("COUPON");
        carParkResponseDTO.setShortTermParking("WHOLE DAY");
        carParkResponseDTO.setFreeParking("NO");
        carParkResponseDTO.setNightParking("YES");
        carParkResponseDTO.setCarParkDecks("0");
        carParkResponseDTO.setGantryHeight("0");
        carParkResponseDTO.setCarParkBasement("0");

        assertEquals("A1", carParkResponseDTO.getCarParkNo());
        assertEquals("SURFACE CAR PARK", carParkResponseDTO.getCarParkType());
        assertEquals("COUPON", carParkResponseDTO.getTypeOfParkingSystem());
        assertEquals("WHOLE DAY", carParkResponseDTO.getShortTermParking());
        assertEquals("NO", carParkResponseDTO.getFreeParking());
        assertEquals("YES", carParkResponseDTO.getNightParking());
        assertEquals("0", carParkResponseDTO.getCarParkDecks());
        assertEquals("0", carParkResponseDTO.getGantryHeight());
        assertEquals("0", carParkResponseDTO.getCarParkBasement());
    }

    @Test
    void testEqualsAndHashCode() {
        CarParkResponseDTO dto1 = new CarParkResponseDTO("Address 1", new BigDecimal("1.0"), new BigDecimal("1.0"), 100, 50);
        CarParkResponseDTO dto2 = new CarParkResponseDTO("Address 1", new BigDecimal("1.0"), new BigDecimal("1.0"), 100, 50);
        CarParkResponseDTO dto3 = new CarParkResponseDTO("Address 2", new BigDecimal("2.0"), new BigDecimal("2.0"), 200, 100);

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        carParkResponseDTO.setAddress("Test Address");
        carParkResponseDTO.setCarParkNo("A1");

        String toString = carParkResponseDTO.toString();
        assertTrue(toString.contains("Test Address"));
        assertTrue(toString.contains("A1"));
    }

    @Test
    void testNullValues() {
        carParkResponseDTO.setAddress(null);
        carParkResponseDTO.setLatitude(null);
        carParkResponseDTO.setLongitude(null);
        carParkResponseDTO.setTotalLots(null);
        carParkResponseDTO.setAvailableLots(null);

        assertNull(carParkResponseDTO.getAddress());
        assertNull(carParkResponseDTO.getLatitude());
        assertNull(carParkResponseDTO.getLongitude());
        assertNull(carParkResponseDTO.getTotalLots());
        assertNull(carParkResponseDTO.getAvailableLots());
    }

    @Test
    void testZeroValues() {
        carParkResponseDTO.setTotalLots(0);
        carParkResponseDTO.setAvailableLots(0);

        assertEquals(0, carParkResponseDTO.getTotalLots());
        assertEquals(0, carParkResponseDTO.getAvailableLots());
    }

    @Test
    void testNegativeValues() {
        carParkResponseDTO.setTotalLots(-1);
        carParkResponseDTO.setAvailableLots(-5);

        assertEquals(-1, carParkResponseDTO.getTotalLots());
        assertEquals(-5, carParkResponseDTO.getAvailableLots());
    }
}
