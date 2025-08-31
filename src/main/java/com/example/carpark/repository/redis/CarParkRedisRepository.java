package com.example.carpark.repository.redis;

import com.example.carpark.dto.response.CarParkResponseDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis repository for car park caching operations
 */
@Repository
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class CarParkRedisRepository {

    private static final Logger logger = LoggerFactory.getLogger(
        CarParkRedisRepository.class
    );

    private static final String NEAREST_CARPARKS_KEY_PREFIX =
        "carpark:nearest:";
    private static final String ALL_CARPARKS_KEY = "carpark:all";
    private static final String AVAILABLE_CARPARKS_KEY = "carpark:available";
    private static final String CARPARK_DETAIL_KEY_PREFIX = "carpark:detail:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public CarParkRedisRepository(
        RedisTemplate<String, Object> redisTemplate,
        ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Cache nearest car parks for a specific location
     */
    public void cacheNearestCarParks(
        String cacheKey,
        List<CarParkResponseDTO> carParks
    ) {
        try {
            String key = NEAREST_CARPARKS_KEY_PREFIX + cacheKey;
            redisTemplate.opsForValue().set(key, carParks, DEFAULT_TTL);
            logger.debug("Cached nearest car parks for key: {}", key);
        } catch (Exception e) {
            logger.error(
                "Error caching nearest car parks for key: {}",
                cacheKey,
                e
            );
        }
    }

    /**
     * Get cached nearest car parks for a specific location
     */
    public Optional<List<CarParkResponseDTO>> getCachedNearestCarParks(
        String cacheKey
    ) {
        try {
            String key = NEAREST_CARPARKS_KEY_PREFIX + cacheKey;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                List<CarParkResponseDTO> carParks = (List<
                    CarParkResponseDTO
                >) cached;
                logger.debug(
                    "Retrieved cached nearest car parks for key: {}",
                    key
                );
                return Optional.of(carParks);
            }
        } catch (Exception e) {
            logger.error(
                "Error retrieving cached nearest car parks for key: {}",
                cacheKey,
                e
            );
        }
        return Optional.empty();
    }

    /**
     * Cache all car parks
     */
    public void cacheAllCarParks(List<CarParkResponseDTO> carParks) {
        try {
            redisTemplate
                .opsForValue()
                .set(ALL_CARPARKS_KEY, carParks, DEFAULT_TTL);
            logger.debug("Cached all car parks");
        } catch (Exception e) {
            logger.error("Error caching all car parks", e);
        }
    }

    /**
     * Get cached all car parks
     */
    public Optional<List<CarParkResponseDTO>> getCachedAllCarParks() {
        try {
            Object cached = redisTemplate.opsForValue().get(ALL_CARPARKS_KEY);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                List<CarParkResponseDTO> carParks = (List<
                    CarParkResponseDTO
                >) cached;
                logger.debug("Retrieved cached all car parks");
                return Optional.of(carParks);
            }
        } catch (Exception e) {
            logger.error("Error retrieving cached all car parks", e);
        }
        return Optional.empty();
    }

    /**
     * Cache available car parks
     */
    public void cacheAvailableCarParks(List<CarParkResponseDTO> carParks) {
        try {
            redisTemplate
                .opsForValue()
                .set(AVAILABLE_CARPARKS_KEY, carParks, DEFAULT_TTL);
            logger.debug("Cached available car parks");
        } catch (Exception e) {
            logger.error("Error caching available car parks", e);
        }
    }

    /**
     * Get cached available car parks
     */
    public Optional<List<CarParkResponseDTO>> getCachedAvailableCarParks() {
        try {
            Object cached = redisTemplate
                .opsForValue()
                .get(AVAILABLE_CARPARKS_KEY);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                List<CarParkResponseDTO> carParks = (List<
                    CarParkResponseDTO
                >) cached;
                logger.debug("Retrieved cached available car parks");
                return Optional.of(carParks);
            }
        } catch (Exception e) {
            logger.error("Error retrieving cached available car parks", e);
        }
        return Optional.empty();
    }

    /**
     * Cache individual car park details
     */
    public void cacheCarParkDetail(
        String carParkNo,
        CarParkResponseDTO carPark
    ) {
        try {
            String key = CARPARK_DETAIL_KEY_PREFIX + carParkNo;
            redisTemplate.opsForValue().set(key, carPark, DEFAULT_TTL);
            logger.debug("Cached car park detail for: {}", carParkNo);
        } catch (Exception e) {
            logger.error("Error caching car park detail for: {}", carParkNo, e);
        }
    }

    /**
     * Get cached individual car park details
     */
    public Optional<CarParkResponseDTO> getCachedCarParkDetail(
        String carParkNo
    ) {
        try {
            String key = CARPARK_DETAIL_KEY_PREFIX + carParkNo;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                CarParkResponseDTO carPark = (CarParkResponseDTO) cached;
                logger.debug(
                    "Retrieved cached car park detail for: {}",
                    carParkNo
                );
                return Optional.of(carPark);
            }
        } catch (Exception e) {
            logger.error(
                "Error retrieving cached car park detail for: {}",
                carParkNo,
                e
            );
        }
        return Optional.empty();
    }

    /**
     * Invalidate all car park caches
     */
    public void invalidateAllCaches() {
        try {
            redisTemplate.delete(ALL_CARPARKS_KEY);
            redisTemplate.delete(AVAILABLE_CARPARKS_KEY);

            // Delete all nearest car park caches
            Set<String> keys = redisTemplate.keys(
                NEAREST_CARPARKS_KEY_PREFIX + "*"
            );
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

            // Delete all car park detail caches
            keys = redisTemplate.keys(CARPARK_DETAIL_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

            logger.info("Invalidated all car park caches");
        } catch (Exception e) {
            logger.error("Error invalidating car park caches", e);
        }
    }

    /**
     * Invalidate specific car park cache
     */
    public void invalidateCarParkCache(String carParkNo) {
        try {
            String key = CARPARK_DETAIL_KEY_PREFIX + carParkNo;
            redisTemplate.delete(key);

            // Also invalidate nearest car park caches as they might contain this car park
            Set<String> keys = redisTemplate.keys(
                NEAREST_CARPARKS_KEY_PREFIX + "*"
            );
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

            logger.debug("Invalidated cache for car park: {}", carParkNo);
        } catch (Exception e) {
            logger.error(
                "Error invalidating cache for car park: {}",
                carParkNo,
                e
            );
        }
    }

    /**
     * Get cache statistics
     */
    public CacheStats getCacheStats() {
        try {
            long totalKeys = 0;
            long nearestKeys = 0;
            long detailKeys = 0;

            Set<String> allKeys = redisTemplate.keys("carpark:*");
            if (allKeys != null) {
                totalKeys = allKeys.size();

                Set<String> nearestKeysSet = redisTemplate.keys(
                    NEAREST_CARPARKS_KEY_PREFIX + "*"
                );
                if (nearestKeysSet != null) {
                    nearestKeys = nearestKeysSet.size();
                }

                Set<String> detailKeysSet = redisTemplate.keys(
                    CARPARK_DETAIL_KEY_PREFIX + "*"
                );
                if (detailKeysSet != null) {
                    detailKeys = detailKeysSet.size();
                }
            }

            return new CacheStats(totalKeys, nearestKeys, detailKeys);
        } catch (Exception e) {
            logger.error("Error getting cache stats", e);
            return new CacheStats(0, 0, 0);
        }
    }

    /**
     * Cache statistics data class
     */
    public static class CacheStats {

        private final long totalKeys;
        private final long nearestKeys;
        private final long detailKeys;

        public CacheStats(long totalKeys, long nearestKeys, long detailKeys) {
            this.totalKeys = totalKeys;
            this.nearestKeys = nearestKeys;
            this.detailKeys = detailKeys;
        }

        public long getTotalKeys() {
            return totalKeys;
        }

        public long getNearestKeys() {
            return nearestKeys;
        }

        public long getDetailKeys() {
            return detailKeys;
        }
    }
}
