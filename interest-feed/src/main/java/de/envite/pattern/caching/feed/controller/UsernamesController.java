package de.envite.pattern.caching.feed.controller;

import de.envite.pattern.caching.feed.config.WebResourceProperties;
import de.envite.pattern.caching.feed.domain.UserInterestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static de.envite.pattern.caching.feed.support.ResponseEntitySupport.ok;
import static java.util.Map.entry;
import static java.util.Optional.ofNullable;
import static org.springframework.http.CacheControl.maxAge;

@RestController
@RequestMapping("/usernames")
public class UsernamesController {

    private final WebResourceProperties.Usernames.CacheControl cacheControlProperties;
    private final UserInterestRepository userInterestRepository;

    @Autowired
    public UsernamesController(final WebResourceProperties webResourceProperties,
                               final UserInterestRepository userInterestRepository) {
        this.cacheControlProperties = webResourceProperties.getUsernames().getCacheControl();
        this.userInterestRepository = userInterestRepository;
    }

    @GetMapping
    public ResponseEntity<Set<String>> getUsernames(@RequestParam(name = "limit", required = false) final Integer limit) {
        final var usernames = userInterestRepository.getUsernames(ofNullable(limit).orElse(Integer.MAX_VALUE));
        return ok(usernames, entry(cacheControlProperties::isEnabled, builder -> builder.cacheControl(maxAge(cacheControlProperties.getAll()))));
    }
}
