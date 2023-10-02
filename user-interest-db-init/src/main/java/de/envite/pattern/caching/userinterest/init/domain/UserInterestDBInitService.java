package de.envite.pattern.caching.userinterest.init.domain;

import de.envite.pattern.caching.userinterest.init.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.toSet;

@Component
public class UserInterestDBInitService {

    private static final Logger log = LoggerFactory.getLogger(UserInterestDBInitService.class);

    private final AppProperties appProperties;
    private final UsersDatasetService usersDatasetService;
    private final NewsDatasetService newsDatasetService;
    private final RedisTemplate<String, Set<String>> redisTemplate;

    public UserInterestDBInitService(
            @Autowired final AppProperties appProperties,
            @Autowired final UsersDatasetService usersDatasetService,
            @Autowired final NewsDatasetService newsDatasetService,
            @Autowired final RedisTemplate<String, Set<String>> redisTemplate) {
        this.appProperties = appProperties;
        this.usersDatasetService = usersDatasetService;
        this.newsDatasetService = newsDatasetService;
        this.redisTemplate = redisTemplate;
    }

    public void initDB() {
        log.info("Start initializing user interest db");
        final var startTimeMs = System.currentTimeMillis();
        final var users = usersDatasetService.getUsers(appProperties.getUsersCount());
        final var categories = new LinkedList<>(newsDatasetService.getCategories(appProperties.getNewsFromDate(), appProperties.getNewsUntilDate()));
        final Random random = ThreadLocalRandom.current();
        for (final var user : users) {
            shuffle(categories, random);
            final var limit = random.nextInt(appProperties.getMinInterests(), appProperties.getMaxInterests() + 1);
            final var userInterests =  categories.stream().limit(limit).collect(toSet());
            redisTemplate.opsForValue().set(user, userInterests);
        }
        log.info("Completed initializing user interest db with {} users and {} categories in {} ms", users.size(), categories.size(), (System.currentTimeMillis() - startTimeMs));
    }

    public void logCategories() {
        final var categories = new ArrayList<>(newsDatasetService.getCategories(appProperties.getNewsFromDate(), appProperties.getNewsUntilDate()));
        categories.sort(String::compareTo);
        log.info("Categories in interval {}/{}: {} ({})", appProperties.getNewsFromDate(), appProperties.getNewsUntilDate(), categories, categories.size());
    }

}
