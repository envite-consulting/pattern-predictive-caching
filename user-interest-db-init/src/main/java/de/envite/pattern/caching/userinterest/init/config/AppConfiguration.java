package de.envite.pattern.caching.userinterest.init.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class AppConfiguration {

    @Bean
    public RedisTemplate<?, ?> redisTemplate(final RedisConnectionFactory connectionFactory) {
        final var template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
