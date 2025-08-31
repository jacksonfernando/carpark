package com.example.carpark.repository.external;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Repository for handling external API calls related to car parks
 */
@Repository
public class CarParkExternalApiRepository {

    private static final Logger logger = LoggerFactory.getLogger(
        CarParkExternalApiRepository.class
    );

    @Value("${carpark.api.url}")
    private String carparkApiUrl;

    @Value("${carpark.api.key}")
    private String carparkApiKey;

    private final WebClient webClient;
    private final JsonFactory jsonFactory;

    public CarParkExternalApiRepository() {
        this.webClient = WebClient.builder().build();
        this.jsonFactory = new JsonFactory();
    }

    /**
     * Fetch car park availability data from external API using true streaming
     * @param consumer Consumer to process each car park data item
     */
    public void fetchCarParkAvailabilityStreaming(
        Consumer<CarParkAvailabilityData> consumer
    ) {
        try {
            // Get current date time for the API call
            String currentDateTime = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            );

            // Build the API URL without date_time parameter for now
            String apiUrl = carparkApiUrl;

            logger.info("Calling car park API with streaming: {}", apiUrl);

            // Build the WebClient request with proper headers
            WebClient.RequestHeadersSpec<?> request = webClient
                .get()
                .uri(apiUrl);

            // Add API key header if provided
            if (carparkApiKey != null && !carparkApiKey.isEmpty()) {
                request = request.header("X-Api-Key", carparkApiKey);
                logger.info("Using API key for authentication");
            } else {
                logger.warn("No API key provided - using public access");
            }

            // Use true streaming with DataBuffer
            request
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .timeout(java.time.Duration.ofSeconds(120))
                .reduce(new StringBuilder(), (sb, dataBuffer) -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    sb.append(new String(bytes, StandardCharsets.UTF_8));
                    return sb;
                })
                .map(StringBuilder::toString)
                .doOnNext(response -> {
                    if (response == null || response.isEmpty()) {
                        logger.error("Empty response from car park API");
                        return;
                    }

                    logger.info(
                        "Received streaming response, length: {}",
                        response.length()
                    );
                    logger.debug(
                        "Response preview: {}",
                        response.substring(0, Math.min(200, response.length()))
                    );

                    // Process the response using streaming JSON parser
                    logger.info("Starting JSON streaming processing...");
                    try {
                        processJsonStreaming(response, consumer);
                        logger.info("Completed JSON streaming processing");
                    } catch (Exception e) {
                        logger.error("Error processing JSON stream", e);
                        throw new RuntimeException(
                            "Failed to process JSON stream",
                            e
                        );
                    }
                })
                .block();
        } catch (Exception e) {
            logger.error("Error fetching car park availability", e);
            throw new RuntimeException(
                "Failed to fetch car park availability",
                e
            );
        }
    }

    /**
     * Process JSON using Jackson streaming API to avoid loading entire response into memory
     */
    private void processJsonStreaming(
        String jsonResponse,
        Consumer<CarParkAvailabilityData> consumer
    ) throws Exception {
        int processedCount = 0;
        logger.info(
            "Starting JSON streaming processing with response length: {}",
            jsonResponse.length()
        );

        try (
            JsonParser parser = jsonFactory.createParser(
                new StringReader(jsonResponse)
            )
        ) {
            // Navigate to carpark_data array
            while (parser.nextToken() != null) {
                if (
                    parser.getCurrentToken() == JsonToken.FIELD_NAME &&
                    "carpark_data".equals(parser.getCurrentName())
                ) {
                    // Start of carpark_data array
                    if (parser.nextToken() == JsonToken.START_ARRAY) {
                        logger.info(
                            "Found carpark_data array, starting streaming processing"
                        );

                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            if (
                                parser.getCurrentToken() ==
                                JsonToken.START_OBJECT
                            ) {
                                CarParkAvailabilityData data =
                                    parseCarParkFromStream(parser);
                                if (data != null) {
                                    consumer.accept(data);
                                    processedCount++;
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }

        logger.info(
            "Streaming processing completed. Total processed: {}",
            processedCount
        );
    }

    /**
     * Parse a single car park from the JSON stream
     */
    private CarParkAvailabilityData parseCarParkFromStream(JsonParser parser)
        throws Exception {
        String carparkNumber = null;
        int totalLots = 0;
        int availableLots = 0;
        String lotType = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
                String fieldName = parser.getCurrentName();

                switch (fieldName) {
                    case "carpark_number":
                        parser.nextToken();
                        carparkNumber = parser.getValueAsString();
                        break;
                    case "carpark_info":
                        if (parser.nextToken() == JsonToken.START_ARRAY) {
                            // Get first carpark_info object
                            if (parser.nextToken() == JsonToken.START_OBJECT) {
                                while (
                                    parser.nextToken() != JsonToken.END_OBJECT
                                ) {
                                    if (
                                        parser.getCurrentToken() ==
                                        JsonToken.FIELD_NAME
                                    ) {
                                        String infoField =
                                            parser.getCurrentName();
                                        parser.nextToken();

                                        switch (infoField) {
                                            case "total_lots":
                                                totalLots = Integer.parseInt(
                                                    parser.getValueAsString()
                                                );
                                                break;
                                            case "lots_available":
                                                availableLots =
                                                    Integer.parseInt(
                                                        parser.getValueAsString()
                                                    );
                                                break;
                                            case "lot_type":
                                                lotType =
                                                    parser.getValueAsString();
                                                break;
                                        }
                                    }
                                }
                            }
                            // Skip to end of carpark_info array
                            while (
                                parser.getCurrentToken() != JsonToken.END_ARRAY
                            ) {
                                parser.nextToken();
                            }
                        }
                        break;
                }
            }
        }

        if (carparkNumber != null && lotType != null) {
            return new CarParkAvailabilityData(
                carparkNumber,
                totalLots,
                availableLots,
                lotType
            );
        }

        return null;
    }

    /**
     * Data class to hold car park availability information
     */
    public static class CarParkAvailabilityData {

        private final String carparkNumber;
        private final int totalLots;
        private final int availableLots;
        private final String lotType;

        public CarParkAvailabilityData(
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
    }
}
