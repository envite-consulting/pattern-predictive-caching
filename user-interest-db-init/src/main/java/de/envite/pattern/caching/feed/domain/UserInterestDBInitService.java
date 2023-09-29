package de.envite.pattern.caching.feed.domain;

import de.envite.pattern.caching.feed.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.toSet;
import static org.springframework.data.redis.core.ScanOptions.scanOptions;

@Component
public class UserInterestDBInitService {

    private static final Logger log = LoggerFactory.getLogger(UserInterestDBInitService.class);

    private final AppProperties appProperties;
    private final UsersDatasetService usersDatasetService;
    private final NewsDatasetService newsDatasetService;
    private final RedisTemplate<String, Set<String>> redisTemplate;
    private final Random random;

    public UserInterestDBInitService(
            @Autowired final AppProperties appProperties,
            @Autowired final UsersDatasetService usersDatasetService,
            @Autowired final NewsDatasetService newsDatasetService,
            @Autowired final RedisTemplate<String, Set<String>> redisTemplate,
            @Autowired final Random random) {
        this.appProperties = appProperties;
        this.usersDatasetService = usersDatasetService;
        this.newsDatasetService = newsDatasetService;
        this.redisTemplate = redisTemplate;
        this.random = random;
    }

    public void initDB() {
        log.info("Start initializing user interest db");
        final long startTimeMs = System.currentTimeMillis();
        final List<String> users = usersDatasetService.getUsers().stream().limit(appProperties.getUsersCount()).toList();
        final List<String> categories = new LinkedList<>(newsDatasetService.getCategories());
        for (final String user : users) {
            shuffle(categories);
            final int limit = random.nextInt(appProperties.getMinInterests(), appProperties.getMaxInterests() + 1);
            final Set<String> userInterests =  categories.stream().limit(limit).collect(toSet());
            redisTemplate.opsForValue().set(user, userInterests);
        }
        log.info("Completed initializing user interest db in {} ms", (System.currentTimeMillis() - startTimeMs));
    }

    public void logDB() {
        log.info("Log all existing keys in db");
        Set<String> allKeys;
        try(final Cursor<String> keyCursor = redisTemplate.scan(scanOptions().type(DataType.STRING).build())) {
            allKeys = keyCursor.stream().collect(toSet());
        }
        for(final String key : allKeys) {
            final Set<String> value = redisTemplate.opsForValue().get(key);
            log.info("{} -> {}", key, value);
        }
    }
}
