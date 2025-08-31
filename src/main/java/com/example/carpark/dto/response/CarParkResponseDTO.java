package com.example.carpark.dto.response;

import java.math.BigDecimal;
import java.util.Objects;

public class CarParkResponseDTO {

    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer totalLots;
    private Integer availableLots;

    // Constructors
    public CarParkResponseDTO() {}

    public CarParkResponseDTO(
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer totalLots,
        Integer availableLots
    ) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.totalLots = totalLots;
        this.availableLots = availableLots;
    }

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Integer getTotalLots() {
        return totalLots;
    }

    public void setTotalLots(Integer totalLots) {
        this.totalLots = totalLots;
    }

    public Integer getAvailableLots() {
        return availableLots;
    }

    public void setAvailableLots(Integer availableLots) {
        this.availableLots = availableLots;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarParkResponseDTO that = (CarParkResponseDTO) o;
        return (
            Objects.equals(address, that.address) &&
            Objects.equals(latitude, that.latitude) &&
            Objects.equals(longitude, that.longitude) &&
            Objects.equals(totalLots, that.totalLots) &&
            Objects.equals(availableLots, that.availableLots)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            address,
            latitude,
            longitude,
            totalLots,
            availableLots
        );
    }

    @Override
    public String toString() {
        return (
            "CarParkResponseDTO{" +
            "address='" +
            address +
            '\'' +
            ", latitude=" +
            latitude +
            ", longitude=" +
            longitude +
            ", totalLots=" +
            totalLots +
            ", availableLots=" +
            availableLots +
            '}'
        );
    }
}
