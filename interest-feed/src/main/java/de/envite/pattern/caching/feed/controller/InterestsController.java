package de.envite.pattern.caching.feed.controller;

import de.envite.pattern.caching.feed.config.WebResourceProperties;
import de.envite.pattern.caching.feed.domain.UserInterestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

import static de.envite.pattern.caching.feed.support.ResponseEntitySupport.ok;
import static java.util.Map.entry;
import static java.util.Optional.ofNullable;
import static org.springframework.http.CacheControl.maxAge;

@RestController
@RequestMapping("/interests")
public class InterestsController {

    private final WebResourceProperties.Interests.CacheControl cacheControlProperties;
    private final UserInterestRepository userInterestRepository;

    @Autowired
    public InterestsController(final WebResourceProperties webResourceProperties,
                               final UserInterestRepository userInterestRepository) {
        this.cacheControlProperties = webResourceProperties.getInterests().getCacheControl();
        this.userInterestRepository = userInterestRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String,Set<String>>> getInterests(@RequestParam(name = "limit", required = false) final Integer limit) {
        final var interests = userInterestRepository.getInterests(ofNullable(limit).orElse(Integer.MAX_VALUE));
        return ok(interests, entry(cacheControlProperties::isEnabled, builder -> builder.cacheControl(maxAge(cacheControlProperties.getAll()))));
    }

    @GetMapping("/{username}")
    public ResponseEntity<Set<String>> getInterestsByUser(@PathVariable(name = "username") final String username) {
        final var interests =  userInterestRepository.getInterestsByUser(username);
        return ok(interests, entry(cacheControlProperties::isEnabled, builder -> builder.cacheControl(maxAge(cacheControlProperties.getByUser()))));
    }
}
