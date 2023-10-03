package de.envite.pattern.caching.feed.domain;

import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static org.springframework.data.redis.core.ScanOptions.scanOptions;

@Component
public class UserInterestRepository {

    private final RedisTemplate<String, Set<String>> redisTemplate;

    public UserInterestRepository(@Autowired final RedisTemplate<String, Set<String>> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Timed
    public SortedSet<String> getInterestsByUser(final String username) {
        return ofNullable(redisTemplate.opsForValue().get(username)).map(TreeSet::new).orElseGet(TreeSet::new);
    }

    @Timed
    public SortedSet<String> getUsernames(final int limit) {
        try(final var keyCursor = redisTemplate.scan(scanOptions().type(DataType.STRING).build())) {
            return keyCursor
                    .stream().collect(toCollection(TreeSet::new))
                    .stream().limit(limit).collect(toCollection(TreeSet::new));
        }
    }

    @Timed
    public SortedMap<String,Set<String>> getInterests(final int limit) {
        final var interests = new TreeMap<String,Set<String>>();
        final var usernames = getUsernames(limit);
        for(final var username : usernames) {
            interests.put(username, getInterestsByUser(username));
        }
        return interests;
    }
}
