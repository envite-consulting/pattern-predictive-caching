package de.envite.pattern.caching.feed.controller;

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
public class UsernameController {

    private final UserInterestRepository userInterestRepository;

    @Autowired
    public UsernameController(final UserInterestRepository userInterestRepository) {
        this.userInterestRepository = userInterestRepository;
    }

    @GetMapping
    public Set<String> getUsernames(@RequestParam(name = "limit", required = false) final Integer limit) {
        return userInterestRepository.getUsernames(ofNullable(limit).orElse(Integer.MAX_VALUE));
    }
}
