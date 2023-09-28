package de.envite.pattern.caching.feed;

import de.envite.pattern.caching.feed.domain.UserInterestDBInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserInterestDBInitApplication implements CommandLineRunner {

    @Autowired
    private final UserInterestDBInitService userInterestDbInitService;

    public UserInterestDBInitApplication(@Autowired final UserInterestDBInitService userInterestDbInitService) {
        this.userInterestDbInitService = userInterestDbInitService;
    }

    @Override
    public void run(String... args) throws Exception {
        userInterestDbInitService.inidDB();
        userInterestDbInitService.logDB();
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(UserInterestDBInitApplication.class, args);
    }
}
