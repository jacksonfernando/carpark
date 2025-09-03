package com.example.carpark.repository.external;

import com.example.carpark.common.constants.CarParkConstants;
import com.example.carpark.entity.CarParkAvailability;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
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

    private static final Logger logger = LoggerFactory.getLogger(CarParkExternalApiRepository.class);
    private static final int RESPONSE_PREVIEW_LENGTH = 200;

    @Value("${carpark.api.url}")
    private String carparkApiURL;

    @Value("${carpark.api.key}")
    private String carparkApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public CarParkExternalApiRepository() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetch car park availability data from external API using true streaming
     */
    public void fetchCarParkAvailabilityStreaming(Consumer<CarParkAvailability> consumer) {
        try {
            logger.info("Calling car park API with streaming: {}", carparkApiURL);

            WebClient.RequestHeadersSpec<?> request = webClient.get().uri(carparkApiURL);

            if (carparkApiKey != null && !carparkApiKey.isEmpty()) {
                request = request.header("X-Api-Key", carparkApiKey);
                logger.info("Using API key for authentication");
            }

            request
                    .retrieve()
                    .bodyToFlux(DataBuffer.class)
                    .timeout(java.time.Duration.ofSeconds(CarParkConstants.API_TIMEOUT_SECONDS))
                    .reduce(new StringBuilder(), this::processDataBuffer)
                    .map(StringBuilder::toString)
                    .doOnNext(response -> processJsonResponse(response, consumer))
                    .block();
        } catch (Exception e) {
            logger.error("Error fetching car park availability", e);
            throw new RuntimeException("Failed to fetch car park availability", e);
        }
    }

    /**
     * Process DataBuffer and append to StringBuilder
     */
    private StringBuilder processDataBuffer(StringBuilder sb, DataBuffer dataBuffer) {
        byte[] bytes = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(bytes);
        DataBufferUtils.release(dataBuffer);
        sb.append(new String(bytes, StandardCharsets.UTF_8));
        return sb;
    }

    /**
     * Process JSON response and extract car park data
     */
    private void processJsonResponse(String response, Consumer<CarParkAvailability> consumer) {
        if (response == null || response.isEmpty()) {
            return;
        }

        logger.info("Received streaming response, length: {}", response.length());
        logger.debug("Response preview: {}",
                response.substring(0, Math.min(RESPONSE_PREVIEW_LENGTH, response.length())));
        logger.info("Starting JSON streaming processing...");

        try {
            processJsonStreaming(response, consumer);
            logger.info("Completed JSON streaming processing");
        } catch (Exception e) {
            logger.error("Error processing JSON stream", e);
            throw new RuntimeException("Failed to process JSON stream", e);
        }
    }

    /**
     * Process JSON using Jackson ObjectMapper to avoid loading entire response into
     * memory
     */
    private void processJsonStreaming(String jsonResponse, Consumer<CarParkAvailability> consumer) throws Exception {
        int processedCount = 0;
        logger.info("Starting JSON streaming processing with response length: {}", jsonResponse.length());

        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode itemsNode = rootNode.get("items");

        if (itemsNode != null && itemsNode.isArray() && itemsNode.size() > 0) {
            JsonNode firstItem = itemsNode.get(0);
            JsonNode carparkDataNode = firstItem.get("carpark_data");

            if (carparkDataNode != null && carparkDataNode.isArray()) {
                logger.info("Found carpark_data array with {} items", carparkDataNode.size());
                processedCount = processCarparkDataArray(carparkDataNode, consumer);
            } else {
                logger.warn("carpark_data not found or not an array");
            }
        } else {
            logger.warn("items not found or empty");
        }

        logger.info("Streaming processing completed. Total processed: {}", processedCount);
    }

    /**
     * Process carpark_data array and extract car park information
     */
    private int processCarparkDataArray(JsonNode carparkDataNode, Consumer<CarParkAvailability> consumer) {
        int processedCount = 0;

        for (JsonNode carparkNode : carparkDataNode) {
            try {
                CarParkAvailability data = parseCarParkFromNode(carparkNode);
                if (data != null) {
                    logger.debug("Successfully parsed car park: {}", data.getCarparkNumber());
                    consumer.accept(data);
                    processedCount++;
                }
            } catch (Exception e) {
                logger.warn("Failed to parse car park node: {}", e.getMessage());
            }
        }

        return processedCount;
    }

    /**
     * Parse a single car park from a JsonNode
     */
    private CarParkAvailability parseCarParkFromNode(JsonNode carparkNode) {
        try {
            CarParkAvailability carParkAvailability = new CarParkAvailability();

            // Get carpark_number
            String carparkNumber = extractStringValue(carparkNode, "carpark_number");
            if (carparkNumber == null) {
                logger.warn("carpark_number not found or not a string");
                return null;
            }
            carParkAvailability.setCarparkNumber(carparkNumber);
            logger.debug("Set carpark_number: {}", carparkNumber);

            // Get carpark_info array
            JsonNode carparkInfoNode = carparkNode.get("carpark_info");
            if (carparkInfoNode != null && carparkInfoNode.isArray() && carparkInfoNode.size() > 0) {
                JsonNode firstInfo = carparkInfoNode.get(0);
                extractCarParkInfo(firstInfo, carParkAvailability);
            } else {
                logger.warn("carpark_info not found or empty");
                return null;
            }

            logger.debug("Parsed car park: carparkNumber={}, lotType={}, totalLots={}, availableLots={}",
                    carParkAvailability.getCarparkNumber(),
                    carParkAvailability.getLotType(),
                    carParkAvailability.getTotalLots(),
                    carParkAvailability.getAvailableLots());

            // Check if we have the required data
            if (carParkAvailability.getCarparkNumber() != null && carParkAvailability.getLotType() != null) {
                logger.debug("✅ Valid car park data, returning: {}", carParkAvailability.getCarparkNumber());
                return carParkAvailability;
            }

            logger.warn("❌ Invalid car park data - missing required fields: carparkNumber={}, lotType={}",
                    carParkAvailability.getCarparkNumber(), carParkAvailability.getLotType());
            return null;

        } catch (Exception e) {
            logger.error("Error parsing car park node", e);
            return null;
        }
    }

    /**
     * Extract string value from JsonNode
     */
    private String extractStringValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return (fieldNode != null && fieldNode.isTextual()) ? fieldNode.asText() : null;
    }

    /**
     * Extract car park information from carpark_info node
     */
    private void extractCarParkInfo(JsonNode infoNode, CarParkAvailability carParkAvailability) {
        // Get total_lots
        String totalLotsStr = extractStringValue(infoNode, "total_lots");
        if (totalLotsStr != null) {
            try {
                int totalLots = Integer.parseInt(totalLotsStr);
                carParkAvailability.setTotalLots(totalLots);
                logger.debug("Set total_lots: {}", totalLots);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse total_lots: {}", totalLotsStr);
            }
        }

        // Get lots_available
        String lotsAvailableStr = extractStringValue(infoNode, "lots_available");
        if (lotsAvailableStr != null) {
            try {
                int lotsAvailable = Integer.parseInt(lotsAvailableStr);
                carParkAvailability.setAvailableLots(lotsAvailable);
                logger.debug("Set lots_available: {}", lotsAvailable);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse lots_available: {}", lotsAvailableStr);
            }
        }

        // Get lot_type
        String lotType = extractStringValue(infoNode, "lot_type");
        if (lotType != null) {
            carParkAvailability.setLotType(lotType);
            logger.debug("Set lot_type: {}", lotType);
        }
    }
}
