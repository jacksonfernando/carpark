package com.example.carpark.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "car_parks")
@EntityListeners(AuditingEntityListener.class)
public class CarPark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "car_park_no", unique = true, nullable = false, length = 50)
    private String carParkNo;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "location", columnDefinition = "POINT SRID 4326")
    @JdbcTypeCode(SqlTypes.GEOMETRY)
    private Point location;

    @Column(name = "total_lots")
    private Integer totalLots = 0;

    @Column(name = "available_lots")
    private Integer availableLots = 0;

    @Column(name = "car_park_type", length = 100)
    private String carParkType;

    @Column(name = "type_of_parking_system", length = 100)
    private String typeOfParkingSystem;

    @Column(name = "short_term_parking", length = 100)
    private String shortTermParking;

    @Column(name = "free_parking", length = 100)
    private String freeParking;

    @Column(name = "night_parking", length = 100)
    private String nightParking;

    @Column(name = "car_park_decks", length = 50)
    private String carParkDecks;

    @Column(name = "gantry_height", length = 50)
    private String gantryHeight;

    @Column(name = "car_park_basement", length = 10)
    private String carParkBasement;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(
        name = "created_by",
        nullable = false,
        updatable = false,
        length = 100
    )
    private String createdBy = "SYSTEM";

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy = "SYSTEM";

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Constructors
    public CarPark() {}

    public CarPark(
        String carParkNo,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        this.carParkNo = carParkNo;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public CarPark(
        String carParkNo,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer totalLots,
        Integer availableLots,
        String carParkType,
        String typeOfParkingSystem,
        String shortTermParking,
        String freeParking,
        String nightParking,
        String carParkDecks,
        String gantryHeight,
        String carParkBasement
    ) {
        this.carParkNo = carParkNo;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.totalLots = totalLots;
        this.availableLots = availableLots;
        this.carParkType = carParkType;
        this.typeOfParkingSystem = typeOfParkingSystem;
        this.shortTermParking = shortTermParking;
        this.freeParking = freeParking;
        this.nightParking = nightParking;
        this.carParkDecks = carParkDecks;
        this.gantryHeight = gantryHeight;
        this.carParkBasement = carParkBasement;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCarParkNo() {
        return carParkNo;
    }

    public void setCarParkNo(String carParkNo) {
        this.carParkNo = carParkNo;
    }

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

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
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

    public String getCarParkType() {
        return carParkType;
    }

    public void setCarParkType(String carParkType) {
        this.carParkType = carParkType;
    }

    public String getTypeOfParkingSystem() {
        return typeOfParkingSystem;
    }

    public void setTypeOfParkingSystem(String typeOfParkingSystem) {
        this.typeOfParkingSystem = typeOfParkingSystem;
    }

    public String getShortTermParking() {
        return shortTermParking;
    }

    public void setShortTermParking(String shortTermParking) {
        this.shortTermParking = shortTermParking;
    }

    public String getFreeParking() {
        return freeParking;
    }

    public void setFreeParking(String freeParking) {
        this.freeParking = freeParking;
    }

    public String getNightParking() {
        return nightParking;
    }

    public void setNightParking(String nightParking) {
        this.nightParking = nightParking;
    }

    public String getCarParkDecks() {
        return carParkDecks;
    }

    public void setCarParkDecks(String carParkDecks) {
        this.carParkDecks = carParkDecks;
    }

    public String getGantryHeight() {
        return gantryHeight;
    }

    public void setGantryHeight(String gantryHeight) {
        this.gantryHeight = gantryHeight;
    }

    public String getCarParkBasement() {
        return carParkBasement;
    }

    public void setCarParkBasement(String carParkBasement) {
        this.carParkBasement = carParkBasement;
    }

    // Audit field getters and setters
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    // Soft delete method
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    // Check if entity is deleted
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarPark carPark = (CarPark) o;
        return (
            Objects.equals(id, carPark.id) &&
            Objects.equals(carParkNo, carPark.carParkNo)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, carParkNo);
    }

    @Override
    public String toString() {
        return (
            "CarPark{" +
            "id=" +
            id +
            ", carParkNo='" +
            carParkNo +
            '\'' +
            ", address='" +
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
