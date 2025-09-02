package com.example.carpark.entity;

public class CarParkAvailability {

    private String carparkNumber;
    
    private int totalLots;
    private int availableLots;
    private String lotType;

    // Default constructor
    public CarParkAvailability() {}

    public CarParkAvailability(
        String carparkNumber,
        int totalLots,
        int availableLots,
        String lotType
    ) {
        this.carparkNumber = carparkNumber;
        this.totalLots = totalLots;
        this.availableLots = availableLots;
        this.lotType = lotType;
    }

    public String getCarparkNumber() {
        return carparkNumber;
    }

    public int getTotalLots() {
        return totalLots;
    }

    public int getAvailableLots() {
        return availableLots;
    }

    public String getLotType() {
        return lotType;
    }

    public void setCarparkNumber(String carparkNumber) {
        this.carparkNumber = carparkNumber;
    }

    public void setTotalLots(int totalLots) {
        this.totalLots = totalLots;
    }

    public void setAvailableLots(int availableLots) {
        this.availableLots = availableLots;
    }

    public void setLotType(String lotType) {
        this.lotType = lotType;
    }
}
