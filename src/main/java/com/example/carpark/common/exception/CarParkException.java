package com.example.carpark.common.exception;

/**
 * Custom exception for car park related errors
 */
public class CarParkException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public CarParkException(String message) {
        super(message);
        this.errorCode = "CARPARK_ERROR";
        this.httpStatus = 500;
    }

    public CarParkException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 500;
    }

    public CarParkException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public CarParkException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CARPARK_ERROR";
        this.httpStatus = 500;
    }

    public CarParkException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = 500;
    }

    public CarParkException(String message, String errorCode, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
