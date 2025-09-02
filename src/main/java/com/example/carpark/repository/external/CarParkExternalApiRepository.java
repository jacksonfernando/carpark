package com.example.carpark.repository.external;

import com.example.carpark.common.constants.CarParkConstants;
import com.example.carpark.entity.CarParkAvailability;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.StringReader;
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

    private final String CARPARK_NUMBER = "carpark_number";
    private final String CARPARK_INFO = "carkpark_info";
    private final String TOTAL_LOTS = "total_lots";
    private final String LOTS_AVAILABLE = "lots_available";
    private final String LOT_TYPE = "lot_type";

    private static final Logger logger = LoggerFactory.getLogger(
        CarParkExternalApiRepository.class
    );

    @Value("${carpark.api.url}")
    private String carparkApiURL;

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
        Consumer<CarParkAvailability> consumer
    ) {
        try {
            logger.info(
                "Calling car park API with streaming: {}",
                carparkApiURL
            );

            WebClient.RequestHeadersSpec<?> request = webClient
                .get()
                .uri(carparkApiURL);

            if (carparkApiKey != null && !carparkApiKey.isEmpty()) {
                request = request.header("X-Api-Key", carparkApiKey);
                logger.info("Using API key for authentication");
            }

            // Use true streaming with DataBuffer
            request
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .timeout(
                    java.time.Duration.ofSeconds(
                        CarParkConstants.API_TIMEOUT_SECONDS
                    )
                )
                .reduce(new StringBuilder(), (sb, dataBuffer) -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    sb.append(new String(bytes, StandardCharsets.UTF_8));
                    return sb;
                })
                .map(StringBuilder::toString)
                .doOnNext(response -> {
                    boolean emptyResponse =
                        response == null || response.isEmpty();
                    if (emptyResponse) {
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
        Consumer<CarParkAvailability> consumer
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
                                CarParkAvailability data =
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
    private CarParkAvailability parseCarParkFromStream(JsonParser parser)
        throws Exception {
        String carparkNumber = null;
        CarParkAvailability carParkAvailability = new CarParkAvailability();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
                String fieldName = parser.getCurrentName();

                switch (fieldName) {
                    case CARPARK_NUMBER:
                        parser.nextToken();
                        carparkNumber = parser.getValueAsString();
                        carParkAvailability.setCarparkNumber(carparkNumber);
                        break;
                    case CARPARK_INFO:
                        retrieveCarParkInfoFromStream(
                            parser,
                            carParkAvailability
                        );
                        break;
                }
            }
        }

        if (carparkNumber != null && carParkAvailability.getLotType() != null) {
            return carParkAvailability;
        }

        return null;
    }

    private void retrieveCarParkInfoFromStream(
        JsonParser parser,
        CarParkAvailability carParkAvailability
    ) {
        try {
            if (parser.nextToken() == JsonToken.START_ARRAY) {
                if (parser.nextToken() == JsonToken.START_OBJECT) {
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        if (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
                            String infoField = parser.getCurrentName();
                            parser.nextToken();

                            switch (infoField) {
                                case TOTAL_LOTS:
                                    carParkAvailability.setTotalLots(
                                        Integer.parseInt(
                                            parser.getValueAsString()
                                        )
                                    );
                                    break;
                                case LOTS_AVAILABLE:
                                    carParkAvailability.setAvailableLots(
                                        Integer.parseInt(
                                            parser.getValueAsString()
                                        )
                                    );
                                    break;
                                case LOT_TYPE:
                                    carParkAvailability.setLotType(
                                        parser.getValueAsString()
                                    );
                                    break;
                            }
                        }
                    }
                    // Skip to end of carpark_info array
                    while (parser.getCurrentToken() != JsonToken.END_ARRAY) {
                        parser.nextToken();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing carpark info from stream", e);
        }
    }
}
