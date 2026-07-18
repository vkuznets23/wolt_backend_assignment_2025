package com.example.dopc.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.example.dopc.client.dto.DynamicResponse
import com.example.dopc.client.dto.StaticResponse
import java.time.Duration
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisCacheConfig {

    // create a bean that will be used for @Cacheable annotations
    @Bean
    fun cacheManager(
        redisConnectionFactory: RedisConnectionFactory, // how to connect to Redis
        objectMapper: ObjectMapper, // how to serialize and deserialize objects
    ): CacheManager {
        // when we want to cache a StaticResponse, we need to serialize it to JSON
        // when read from Redis, we need to deserialize it from JSON
        val staticSerializer = Jackson2JsonRedisSerializer(objectMapper, StaticResponse::class.java)
        val dynamicSerializer = Jackson2JsonRedisSerializer(objectMapper, DynamicResponse::class.java)
        val stringSerializer = StringRedisSerializer()

        // default configuration for all caches
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer)
            )
            .disableCachingNullValues() // don't cache null values
            .entryTtl(Duration.ofSeconds(60)) // default TTL for all caches

        // configure specific caches
        val cacheConfigs = mapOf(
            "static" to defaultConfig
                .entryTtl(Duration.ofSeconds(120)) // TTL for static cache
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(staticSerializer)
                ),
            "dynamic" to defaultConfig
                .entryTtl(Duration.ofSeconds(30)) // TTL for dynamic cache
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(dynamicSerializer)
                )
        )

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build()
    }
}