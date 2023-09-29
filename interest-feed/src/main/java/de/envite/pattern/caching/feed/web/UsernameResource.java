package de.envite.pattern.caching.feed.web;

import de.envite.pattern.caching.feed.domain.UserInterestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static java.util.Optional.ofNullable;

@RestController
@RequestMapping("/usernames")
public class UsernameResource {

    private final UserInterestRepository userInterestRepository;

    public UsernameResource(@Autowired final UserInterestRepository userInterestRepository) {
        this.userInterestRepository = userInterestRepository;
    }

    @GetMapping
    public Set<String> getUsernames(@RequestParam(name = "limit", required = false) final Integer limit) {
        return userInterestRepository.getUsernames(ofNullable(limit).orElse(Integer.MAX_VALUE));
    }
}
