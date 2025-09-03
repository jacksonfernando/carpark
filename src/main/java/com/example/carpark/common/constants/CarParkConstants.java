package com.example.carpark.common.constants;

/**
 * Constants for car park related operations
 */
public final class CarParkConstants {

    private CarParkConstants() {
        // Private constructor to prevent instantiation
    }

    // API Endpoints
    public static final String API_BASE_PATH = "/v1/carparks";
    public static final String NEAREST_ENDPOINT = "/nearest";
    public static final String IMPORT_ENDPOINT = "/import";
    public static final String UPDATE_AVAILABILITY_ENDPOINT = "/update-availability";
    public static final String HEALTH_ENDPOINT = "/health";

    // External API Configuration
    // TODO: External URL should be in .env or application properties
    public static final String EXTERNAL_API_URL = "https://api.data.gov.sg/v1/transport/carpark-availability";
    public static final String API_KEY_HEADER = "X-Api-Key";
    public static final int API_TIMEOUT_SECONDS = 120;

    // Database Configuration
    public static final String SYSTEM_USER = "SYSTEM";
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    public static final double DEFAULT_SEARCH_RADIUS_KM = 10.0;
    public static final double MAX_SEARCH_RADIUS_KM = 50.0;

    // Car Park Types
    public static final String CAR_PARK_TYPE_CAR = "C";
    public static final String CAR_PARK_TYPE_MOTORCYCLE = "Y";
    public static final String CAR_PARK_TYPE_HEAVY_VEHICLE = "H";

    // CSV Import Configuration
    public static final int CSV_BATCH_SIZE = 100;
    public static final String CSV_ENCODING = "UTF-8";

    // Cache Configuration
    public static final int CACHE_TTL_MINUTES = 15;

    // Error Messages
    public static final String ERROR_INVALID_COORDINATES = "Invalid coordinates provided";
    public static final String ERROR_INVALID_PAGE_PARAMETERS = "Invalid page parameters";
    public static final String ERROR_EXTERNAL_API_FAILED = "External API call failed";
    public static final String ERROR_DATABASE_OPERATION_FAILED = "Database operation failed";
    public static final String ERROR_CSV_IMPORT_FAILED = "CSV import failed";
    public static final String ERROR_AVAILABILITY_UPDATE_FAILED = "Availability update failed";

    // Success Messages
    public static final String SUCCESS_IMPORT_COMPLETED = "Car park data streaming import completed successfully";
    public static final String SUCCESS_AVAILABILITY_UPDATED = "Car park availability update completed successfully";
    public static final String SUCCESS_HEALTH_CHECK = "Car Park API is running";

    // Validation Messages
    public static final String VALIDATION_LATITUDE_RANGE = "Latitude must be between -90 and 90";
    public static final String VALIDATION_LONGITUDE_RANGE = "Longitude must be between -180 and 180";
    public static final String VALIDATION_PAGE_SIZE_RANGE = "Page size must be between 1 and " + MAX_PAGE_SIZE;
    public static final String VALIDATION_SEARCH_RADIUS_RANGE = "Search radius must be between 0.1 and "
            + MAX_SEARCH_RADIUS_KM + " km";
}
