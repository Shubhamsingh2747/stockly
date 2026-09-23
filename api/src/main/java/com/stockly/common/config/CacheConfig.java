package com.stockly.common.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PRODUCTS = "products";
    public static final String PRODUCT_LISTS = "productLists";
    public static final String CATEGORIES = "categories";

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public CacheManager cacheManager(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port) {
        if (isRedisReachable(host, port)) {
            try {
                LettuceConnectionFactory factory = new LettuceConnectionFactory(
                        new RedisStandaloneConfiguration(host, port));
                factory.afterPropertiesSet();
                RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(5))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));
                log.info("Product cache using Redis at {}:{}", host, port);
                return RedisCacheManager.builder(factory).cacheDefaults(defaults).build();
            } catch (Exception ex) {
                log.warn("Redis cache setup failed, falling back to in-memory: {}", ex.getMessage());
            }
        } else {
            log.warn("Redis is not running at {}:{} — using in-memory cache. Install Redis later to enable shared cache.",
                    host, port);
        }
        return new ConcurrentMapCacheManager(PRODUCTS, PRODUCT_LISTS, CATEGORIES);
    }

    private boolean isRedisReachable(String host, int port) {
        try {
            RedisClient client = RedisClient.create(RedisURI.builder()
                    .withHost(host)
                    .withPort(port)
                    .withTimeout(Duration.ofMillis(400))
                    .build());
            try (var connection = client.connect()) {
                connection.sync().ping();
            }
            client.shutdown();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
