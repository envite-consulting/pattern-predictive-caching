package de.envite.pattern.caching.feed.web;

import de.envite.pattern.caching.feed.domain.UserInterestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;

@RestController
@RequestMapping("/interests")
public class InterestResource {

    private final UserInterestRepository userInterestRepository;

    public InterestResource(@Autowired final UserInterestRepository userInterestRepository) {
        this.userInterestRepository = userInterestRepository;
    }

    @GetMapping
    public Map<String,Set<String>> getInterests(@RequestParam(name = "limit", required = false) final Integer limit) {
        return userInterestRepository.getInterests(ofNullable(limit).orElse(Integer.MAX_VALUE));
    }

    @GetMapping(path = "/{username}")
    public Set<String> getInterestsByUser(@PathVariable final String username) {
        return userInterestRepository.getInterestsByUser(username);
    }
}
