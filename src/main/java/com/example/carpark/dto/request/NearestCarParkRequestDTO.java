package com.example.carpark.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class NearestCarParkRequestDTO {

    @NotNull(message = "Latitude is required")
    @DecimalMin(
        value = "-90.0",
        message = "Latitude must be between -90 and 90"
    )
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(
        value = "-180.0",
        message = "Longitude must be between -180 and 180"
    )
    @DecimalMax(
        value = "180.0",
        message = "Longitude must be between -180 and 180"
    )
    private BigDecimal longitude;

    @Min(value = 1, message = "Page must be at least 1")
    private Integer page = 1;

    @Min(value = 1, message = "Per page must be at least 1")
    @Max(value = 100, message = "Per page must not exceed 100")
    private Integer perPage = 10;

    // Constructors
    public NearestCarParkRequestDTO() {}

    public NearestCarParkRequestDTO(
        BigDecimal latitude,
        BigDecimal longitude,
        Integer page,
        Integer perPage
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.page = page;
        this.perPage = perPage;
    }

    // Getters and Setters
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

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPerPage() {
        return perPage;
    }

    public void setPerPage(Integer perPage) {
        this.perPage = perPage;
    }
}
