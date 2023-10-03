package de.envite.pattern.caching.feed.web;

import de.envite.pattern.caching.feed.domain.FeedEntry;
import de.envite.pattern.caching.feed.domain.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static java.util.Optional.ofNullable;

@RestController
@RequestMapping("/feed")
public class FeedResource {

    private final FeedService feedService;

    public FeedResource(@Autowired final FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping(path = {"/{username}", "/", ""})
    public List<FeedEntry> getFeedByUser(@PathVariable(name = "username", required = false) final String username,
                                         @RequestParam(name = "date", required = false) final LocalDate date) {
        return feedService.getFeedByUser(ofNullable(username).orElse(""), ofNullable(date).orElseGet(LocalDate::now));
    }
}
