package com.example.carpark.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CarParkTest {

    private CarPark carPark;

    @BeforeEach
    void setUp() {
        carPark = new CarPark();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(carPark);
        assertNull(carPark.getCarParkNo());
        assertNull(carPark.getAddress());
        assertNull(carPark.getLatitude());
        assertNull(carPark.getLongitude());
        assertEquals(0, carPark.getTotalLots());
        assertEquals(0, carPark.getAvailableLots());
    }

    @Test
    void testParameterizedConstructor() {
        String carParkNo = "A1";
        String address = "Test Address";
        BigDecimal latitude = new BigDecimal("1.23456789");
        BigDecimal longitude = new BigDecimal("103.45678901");

        CarPark testCarPark = new CarPark(carParkNo, address, latitude, longitude);

        assertEquals(carParkNo, testCarPark.getCarParkNo());
        assertEquals(address, testCarPark.getAddress());
        assertEquals(latitude, testCarPark.getLatitude());
        assertEquals(longitude, testCarPark.getLongitude());
    }

    @Test
    void testSettersAndGetters() {
        // Basic fields
        carPark.setId(1L);
        carPark.setCarParkNo("A1");
        carPark.setAddress("Test Address");
        carPark.setLatitude(new BigDecimal("1.23456789"));
        carPark.setLongitude(new BigDecimal("103.45678901"));
        carPark.setTotalLots(100);
        carPark.setAvailableLots(50);

        assertEquals(1L, carPark.getId());
        assertEquals("A1", carPark.getCarParkNo());
        assertEquals("Test Address", carPark.getAddress());
        assertEquals(new BigDecimal("1.23456789"), carPark.getLatitude());
        assertEquals(new BigDecimal("103.45678901"), carPark.getLongitude());
        assertEquals(100, carPark.getTotalLots());
        assertEquals(50, carPark.getAvailableLots());

        // Additional fields
        carPark.setCarParkType("SURFACE CAR PARK");
        carPark.setTypeOfParkingSystem("COUPON");
        carPark.setShortTermParking("WHOLE DAY");
        carPark.setFreeParking("NO");
        carPark.setNightParking("YES");
        carPark.setCarParkDecks("0");
        carPark.setGantryHeight("0");
        carPark.setCarParkBasement("0");

        assertEquals("SURFACE CAR PARK", carPark.getCarParkType());
        assertEquals("COUPON", carPark.getTypeOfParkingSystem());
        assertEquals("WHOLE DAY", carPark.getShortTermParking());
        assertEquals("NO", carPark.getFreeParking());
        assertEquals("YES", carPark.getNightParking());
        assertEquals("0", carPark.getCarParkDecks());
        assertEquals("0", carPark.getGantryHeight());
        assertEquals("0", carPark.getCarParkBasement());
    }

    @Test
    void testAuditFields() {
        LocalDateTime now = LocalDateTime.now();
        String user = "TEST_USER";

        carPark.setCreatedAt(now);
        carPark.setCreatedBy(user);
        carPark.setUpdatedAt(now);
        carPark.setUpdatedBy(user);

        assertEquals(now, carPark.getCreatedAt());
        assertEquals(user, carPark.getCreatedBy());
        assertEquals(now, carPark.getUpdatedAt());
        assertEquals(user, carPark.getUpdatedBy());
    }

    @Test
    void testSoftDelete() {
        assertFalse(carPark.isDeleted());
        assertNull(carPark.getDeletedAt());

        carPark.softDelete();

        assertTrue(carPark.isDeleted());
        assertNotNull(carPark.getDeletedAt());
    }

    @Test
    void testRestoreSoftDelete() {
        carPark.softDelete();
        assertTrue(carPark.isDeleted());

        carPark.setDeletedAt(null);
        assertFalse(carPark.isDeleted());
    }

    @Test
    void testDefaultValues() {
        CarPark newCarPark = new CarPark();
        assertEquals(0, newCarPark.getTotalLots());
        assertEquals(0, newCarPark.getAvailableLots());
        assertEquals("SYSTEM", newCarPark.getCreatedBy());
        assertEquals("SYSTEM", newCarPark.getUpdatedBy());
    }

    @Test
    void testEqualsAndHashCode() {
        CarPark carPark1 = new CarPark("A1", "Address 1", new BigDecimal("1.0"), new BigDecimal("1.0"));
        CarPark carPark2 = new CarPark("A1", "Address 1", new BigDecimal("1.0"), new BigDecimal("1.0"));
        CarPark carPark3 = new CarPark("A2", "Address 2", new BigDecimal("2.0"), new BigDecimal("2.0"));

        carPark1.setId(1L);
        carPark2.setId(1L);
        carPark3.setId(2L);

        assertEquals(carPark1, carPark2);
        assertNotEquals(carPark1, carPark3);
        assertEquals(carPark1.hashCode(), carPark2.hashCode());
        assertNotEquals(carPark1.hashCode(), carPark3.hashCode());
    }

    @Test
    void testToString() {
        carPark.setCarParkNo("A1");
        carPark.setAddress("Test Address");

        String toString = carPark.toString();
        assertTrue(toString.contains("A1"));
        assertTrue(toString.contains("Test Address"));
    }
}
