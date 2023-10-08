package de.envite.pattern.caching.userinterest.init.domain;

import de.envite.pattern.caching.userinterest.init.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;
import java.util.random.RandomGenerator;

import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.toSet;

@Service
public class UserInterestDBInitService {

    private static final Logger log = LoggerFactory.getLogger(UserInterestDBInitService.class);

    private final AppProperties appProperties;
    private final UsersDatasetRepository usersDatasetRepository;
    private final NewsDatasetRepository newsDatasetRepository;
    private final RedisTemplate<String, Set<String>> redisTemplate;

    @Autowired
    public UserInterestDBInitService(
            final AppProperties appProperties,
            final UsersDatasetRepository usersDatasetRepository,
            final NewsDatasetRepository newsDatasetRepository,
            final RedisTemplate<String, Set<String>> redisTemplate) {
        this.appProperties = appProperties;
        this.usersDatasetRepository = usersDatasetRepository;
        this.newsDatasetRepository = newsDatasetRepository;
        this.redisTemplate = redisTemplate;
    }

    public void initDB(final RandomGenerator random) {
        log.info("Start initializing user interest db");
        final var startTimeMs = System.currentTimeMillis();
        final var users = usersDatasetRepository.getUsers(appProperties.getUsersCount());
        final var categories = new ArrayList<>(newsDatasetRepository.getCategories(appProperties.getNewsFromDate(), appProperties.getNewsUntilDate()));
        for (final var user : users) {
            shuffle(categories, random);
            final var limit = random.nextInt(appProperties.getMinInterests(), appProperties.getMaxInterests() + 1);
            final var userInterests =  categories.stream().limit(limit).collect(toSet());
            redisTemplate.opsForValue().set(user, userInterests);
        }
        log.info("Completed initializing user interest db with {} users and {} categories in {} ms", users.size(), categories.size(), (System.currentTimeMillis() - startTimeMs));
    }

    public void logCategories() {
        final var categories = new ArrayList<>(newsDatasetRepository.getCategories(appProperties.getNewsFromDate(), appProperties.getNewsUntilDate()));
        categories.sort(String::compareTo);
        log.info("Categories in interval {}/{}: {} ({})", appProperties.getNewsFromDate(), appProperties.getNewsUntilDate(), categories, categories.size());
    }

}
