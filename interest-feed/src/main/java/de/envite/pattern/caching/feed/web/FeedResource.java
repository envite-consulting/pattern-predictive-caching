package de.envite.pattern.caching.feed.web;

import de.envite.pattern.caching.feed.domain.FeedEntry;
import de.envite.pattern.caching.feed.domain.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/feed")
public class FeedResource {

    private final FeedService feedService;

    public FeedResource(@Autowired final FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping(path = "/{username}")
    public List<FeedEntry> getFeedByUser(@PathVariable final String username, @RequestParam(name = "date", required = false) final LocalDate date) {
        return feedService.getFeedByUser(username, Optional.ofNullable(date).orElseGet(LocalDate::now));
    }
}
