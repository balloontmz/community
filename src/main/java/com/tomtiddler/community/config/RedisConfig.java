package com.tomtiddler.community.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {
    /**
     * 复写 redis 配置
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);

        //设置 key 的序列化方式
        template.setKeySerializer(RedisSerializer.string());

        //设置 value 的序列化方式
        template.setValueSerializer(RedisSerializer.json());

        //设置 hash 的 key 的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());

        //设置 hash 的 value 的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();//触发配置更新
		return template;
    }
}
