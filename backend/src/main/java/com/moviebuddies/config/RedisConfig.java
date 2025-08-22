package com.moviebuddies.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
     * 기본 ObjectMapper (Swagger 등에서 사용)
     * activateDefaultTyping 없이 사용하여 Swagger와 충돌 방지
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    /**
     * Redis 전용 ObjectMapper
     * activateDefaultTyping을 적용하여 타입 정보 포함한 직렬화
     * Java 8 날짜/시간 타입을 지원하는 ObjectMapper Bean 등록
     * 이 Bean은 GenericJackson2JsonRedisSerializer에서 자동으로 사용
     */
    @Bean
    @Qualifier("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Redis에서 타입 정보를 포함하여 직렬화/역직렬화
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        return objectMapper;
    }

    /**
     * Redis 데이터 저장/조회를 위한 RedisTemplate 설정
     * 직접적인 Redis 조작이 필요한 경우 사용
     *
     * @param connectionFactory Redis 연결 팩토리
     * @param redisObjectMapper Redis 전용 ObjectMapper
     * @return 설정된 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key는 가독성을 위해 문자열로 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Redis 전용 ObjectMapper를 사용하는 GenericJackson2JsonRedisSerializer
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }


    /**
     * Spring Cache 매니저 설정
     * `@Cacheable`, `@CacheEvict` 등의 어노테이션 동작 제어
     *
     * @param connectionFactory Redis 연결 팩토리
     * @param redisObjectMapper Redis 전용 ObjectMapper
     * @return Redis 기반 캐시 매니저
     */
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {

        // Redis 전용 ObjectMapper를 사용하는 Serializer
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))   // 캐시 만료 시간은 10분으로 설정
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
