package de.envite.pattern.caching.userinterest.init;

import de.envite.pattern.caching.userinterest.init.config.AppProperties;
import de.envite.pattern.caching.userinterest.init.domain.UserInterestDBInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Random;

@SpringBootApplication
public class UserInterestDBInitApplication implements CommandLineRunner {

    private final AppProperties appProperties;
    private final UserInterestDBInitService userInterestDbInitService;

    public UserInterestDBInitApplication(@Autowired final AppProperties appProperties,
                                         @Autowired final UserInterestDBInitService userInterestDbInitService) {
        this.appProperties = appProperties;
        this.userInterestDbInitService = userInterestDbInitService;
    }

    @Override
    public void run(String... args) {
        final Random random = new Random(appProperties.getRandomSeed());
        userInterestDbInitService.initDB(random);
        userInterestDbInitService.logCategories();
    }

    public static void main(String[] args) {
        SpringApplication.run(UserInterestDBInitApplication.class, args);
    }
}
