package br.com.frotasPro.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@Profile("prod")
public class RedisCacheConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .entryTtl(Duration.ofMinutes(2))
                .disableCachingNullValues();
    }
}
