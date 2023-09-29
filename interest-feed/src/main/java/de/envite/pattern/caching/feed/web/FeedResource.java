package de.envite.pattern.caching.feed.web;

import de.envite.pattern.caching.feed.domain.FeedEntry;
import de.envite.pattern.caching.feed.domain.FeedService;
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

    @GetMapping(path = "/{user}")
    public List<FeedEntry> getFeedByUser(@PathVariable final String user, @RequestParam(name = "date", required = false) final LocalDate date) {
        return feedService.getFeedByUser(user, Optional.ofNullable(date).orElseGet(LocalDate::now));
    }
}
