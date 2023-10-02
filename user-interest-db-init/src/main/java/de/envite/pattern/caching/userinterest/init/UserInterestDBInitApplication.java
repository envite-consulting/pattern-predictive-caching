package de.envite.pattern.caching.userinterest.init;

import de.envite.pattern.caching.userinterest.init.domain.UserInterestDBInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserInterestDBInitApplication implements CommandLineRunner {

    private final UserInterestDBInitService userInterestDbInitService;

    public UserInterestDBInitApplication(@Autowired final UserInterestDBInitService userInterestDbInitService) {
        this.userInterestDbInitService = userInterestDbInitService;
    }

    @Override
    public void run(String... args) {
        userInterestDbInitService.initDB();
        userInterestDbInitService.logCategories();
    }

    public static void main(String[] args) {
        SpringApplication.run(UserInterestDBInitApplication.class, args);
    }
}
