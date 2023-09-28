package de.envite.pattern.caching.feed.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserInterestService {

    private final RedisTemplate<String, Set<String>> redisTemplate;

    public UserInterestService(@Autowired final RedisTemplate<String, Set<String>> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Set<String> getInterestsByUser(final String user) {
        return redisTemplate.opsForValue().get(user);
    }
}
