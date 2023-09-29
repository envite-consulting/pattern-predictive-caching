package de.envite.pattern.caching.feed.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.springframework.data.redis.core.ScanOptions.scanOptions;

@Component
public class UserInterestRepository {

    private final RedisTemplate<String, Set<String>> redisTemplate;

    public UserInterestRepository(@Autowired final RedisTemplate<String, Set<String>> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Set<String> getInterestsByUser(final String user) {
        return redisTemplate.opsForValue().get(user);
    }

    public Set<String> getUsernames(final int limit) {
        try(final Cursor<String> keyCursor = redisTemplate.scan(scanOptions().type(DataType.STRING).build())) {
            return keyCursor.stream().limit(limit).collect(toSet());
        }
    }
}
