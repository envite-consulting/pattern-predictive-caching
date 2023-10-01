package de.envite.pattern.caching.feed.domain;

import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.springframework.data.redis.core.ScanOptions.scanOptions;

@Component
public class UserInterestRepository {

    private final RedisTemplate<String, Set<String>> redisTemplate;

    public UserInterestRepository(@Autowired final RedisTemplate<String, Set<String>> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Timed
    public Set<String> getInterestsByUser(final String username) {
        return redisTemplate.opsForValue().get(username);
    }

    @Timed
    public Set<String> getUsernames(final int limit) {
        try(final Cursor<String> keyCursor = redisTemplate.scan(scanOptions().type(DataType.STRING).build())) {
            return keyCursor.stream().limit(limit).collect(toSet());
        }
    }

    @Timed
    public Map<String,Set<String>> getInterests(final int limit) {
        final Map<String,Set<String>> interests = new HashMap<>();
        final Set<String> usernames = getUsernames(limit);
        for(final String username : usernames) {
            interests.put(username, getInterestsByUser(username));
        }
        return interests;
    }
}
