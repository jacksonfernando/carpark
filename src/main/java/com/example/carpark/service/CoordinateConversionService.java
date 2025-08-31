package com.example.carpark.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class CoordinateConversionService {

    private static final Logger logger = LoggerFactory.getLogger(
        CoordinateConversionService.class
    );

    @Value("${onemap.api.url}")
    private String onemapApiUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public CoordinateConversionService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Convert SVY21 coordinates to WGS84 using OneMap API
     */
    public BigDecimal[] convertSVY21ToWGS84(double xCoord, double yCoord) {
        try {
            String url = String.format(
                "%s?X=%f&Y=%f",
                onemapApiUrl,
                xCoord,
                yCoord
            );

            String response = webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            if (response == null) {
                throw new RuntimeException(
                    "No response received from OneMap API"
                );
            }

            JsonNode jsonNode = objectMapper.readTree(response);

            if (
                jsonNode.has("results") &&
                jsonNode.get("results").isArray() &&
                jsonNode.get("results").size() > 0
            ) {
                JsonNode result = jsonNode.get("results").get(0);
                double latitude = result.get("latitude").asDouble();
                double longitude = result.get("longitude").asDouble();

                return new BigDecimal[] {
                    BigDecimal.valueOf(latitude).setScale(
                        8,
                        RoundingMode.HALF_UP
                    ),
                    BigDecimal.valueOf(longitude).setScale(
                        8,
                        RoundingMode.HALF_UP
                    ),
                };
            } else {
                throw new RuntimeException(
                    "Invalid response format from OneMap API"
                );
            }
        } catch (Exception e) {
            logger.error(
                "Error converting coordinates: X={}, Y={}",
                xCoord,
                yCoord,
                e
            );
            throw new RuntimeException("Failed to convert coordinates", e);
        }
    }

    /**
     * Calculate distance between two points using Haversine formula
     */
    public double calculateDistance(
        double lat1,
        double lon1,
        double lat2,
        double lon2
    ) {
        final int R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a =
            Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
            Math.cos(Math.toRadians(lat1)) *
            Math.cos(Math.toRadians(lat2)) *
            Math.sin(lonDistance / 2) *
            Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
