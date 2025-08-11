package com.moviebuddies.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 연결 및 캐싱 설정
 * Spring Cache와 Redis를 연동하여 성능 최적화 제공
 */
@Configuration
@EnableCaching  // Spring Cache 기능 활성화
public class RedisConfig {

    /**
     * Redis 데이터 저장/조회를 위한 RedisTemplate 설정
     * 직접적인 Redis 조작이 필요한 경우 사용
     *
     * @param connectionFactory Redis 연결 팩토리
     * @return 설정된 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        // Key는 가독성을 위해 문자열로 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // value는 객체 저장이 가능하도록 JSON으로 직렬화
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Spring Cache 매니저 설정
     * `@Cacheable`, `@CacheEvict` 등의 어노테이션 동작 제어
     *
     * @param connectionFactory Redis 연결 팩토리
     * @return Redis 기반 캐시 매니저
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))   // 캐시 만료 시간은 10분으로 설정
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
