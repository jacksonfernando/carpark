package com.example.carpark.common.exception;

/**
 * Error codes for car park application
 */
public enum ErrorCode {

    // Validation Errors (400)
    INVALID_COORDINATES("INVALID_COORDINATES", 400),
    INVALID_PAGE_PARAMETERS("INVALID_PAGE_PARAMETERS", 400),
    INVALID_SEARCH_RADIUS("INVALID_SEARCH_RADIUS", 400),

    // Not Found Errors (404)
    CAR_PARK_NOT_FOUND("CAR_PARK_NOT_FOUND", 404),
    EXTERNAL_API_NOT_FOUND("EXTERNAL_API_NOT_FOUND", 404),

    // External API Errors (502)
    EXTERNAL_API_FAILED("EXTERNAL_API_FAILED", 502),
    EXTERNAL_API_TIMEOUT("EXTERNAL_API_TIMEOUT", 502),

    // Database Errors (500)
    DATABASE_OPERATION_FAILED("DATABASE_OPERATION_FAILED", 500),
    CSV_IMPORT_FAILED("CSV_IMPORT_FAILED", 500),
    AVAILABILITY_UPDATE_FAILED("AVAILABILITY_UPDATE_FAILED", 500),

    // Cache Errors (500)
    CACHE_OPERATION_FAILED("CACHE_OPERATION_FAILED", 500),

    // General Errors (500)
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", 500),
    UNKNOWN_ERROR("UNKNOWN_ERROR", 500);

    private final String code;
    private final int httpStatus;

    ErrorCode(String code, int httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
